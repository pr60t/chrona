package com.example.chrona.feature.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.map.CityMapMarker
import com.example.chrona.core.map.ChronaWorldMapHeader
import com.example.chrona.core.model.City
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.core.ui.components.TimezoneCard
import com.example.chrona.ui.theme.ChronaThemeTokens
import java.time.Instant

@Composable
fun WorldClockScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onSetAlarmForCity: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val savedCities by CityRepository.savedCities.collectAsState()
    val preferences by PreferencesRepository.preferences.collectAsState()

    var selectedCityId by remember { mutableStateOf<Long?>(null) }
    var showSearchSheet by remember { mutableStateOf(false) }

    // Live clock update every second
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = Instant.now()
        }
    }

    val listState = rememberLazyListState()

    // Smooth map collapse calculation: 0f (fully expanded) to 1f (collapsed)
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
        }
    }

    val mapMarkers = remember(savedCities, selectedCityId, now, preferences.is24HourFormat) {
        savedCities.map { city ->
            CityMapMarker(
                cityId = city.geonameId.toString(),
                cityName = city.cityName,
                formattedTime = city.formattedTime(preferences.is24HourFormat, now),
                utcOffsetLabel = city.formattedOffset(now),
                mapX = city.mapX,
                mapY = city.mapY,
                identityColor = city.identityColor,
                isSelected = city.geonameId == selectedCityId
            )
        }
    }

    val activeSelectedCity = remember(selectedCityId, savedCities) {
        savedCities.find { it.geonameId == selectedCityId }
    }

    Scaffold(
        topBar = {
            ChronaTopBar(
                title = "World Clock",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToScreensaver = onNavigateToScreensaver,
                onNavigateToAbout = onNavigateToAbout
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSearchSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(100.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add place",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Chrona Natural Earth Map Header
            ChronaWorldMapHeader(
                markers = mapMarkers,
                collapseFraction = collapseFraction,
                onMarkerClick = { markerCityId ->
                    val id = markerCityId.toLongOrNull()
                    if (id != null) {
                        selectedCityId = id
                    }
                }
            )

            // Saved Cities List
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedCities, key = { it.geonameId }) { city ->
                    TimezoneCard(
                        city = city,
                        is24Hour = preferences.is24HourFormat,
                        showImage = preferences.showCityImages,
                        referenceInstant = now,
                        onClick = {
                            selectedCityId = city.geonameId
                        }
                    )
                }
            }
        }
    }

    // City Detail Sheet
    if (activeSelectedCity != null) {
        CityDetailSheet(
            city = activeSelectedCity,
            is24Hour = preferences.is24HourFormat,
            onDismiss = { selectedCityId = null },
            onSetAlarmForCity = { city ->
                selectedCityId = null
                onSetAlarmForCity(city)
            },
            onToggleSaveCity = { cityId ->
                CityRepository.toggleSaveCity(cityId)
            }
        )
    }

    // Timezone Search Sheet
    if (showSearchSheet) {
        TimezoneSearchSheet(
            is24Hour = preferences.is24HourFormat,
            onDismiss = { showSearchSheet = false },
            onCitySelected = { city ->
                showSearchSheet = false
                selectedCityId = city.geonameId
            }
        )
    }
}
