package com.example.chrona.feature.worldclock

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.map.CityMapMarker
import com.example.chrona.core.map.ChronaWorldMapHeader
import com.example.chrona.core.model.City
import com.example.chrona.core.ui.components.ChronaFabDefaults
import com.example.chrona.core.ui.components.ChronaFloatingActionButton
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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedCityId by remember { mutableStateOf<Long?>(null) }
    var showSearchSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = Instant.now()
        }
    }

    LaunchedEffect(selectedCityId) {
        val id = selectedCityId ?: return@LaunchedEffect
        val index = savedCities.indexOfFirst { it.geonameId == id }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    val mapMarkers = remember(savedCities, selectedCityId, now, preferences.is24HourFormat) {
        savedCities.filter { !it.isUtc }.map { city ->
            CityMapMarker(
                cityId = city.geonameId.toString(),
                cityName = city.cityName,
                formattedTime = city.formattedTime(preferences.is24HourFormat, now),
                utcOffsetLabel = city.formattedOffset(),
                mapX = city.mapX,
                mapY = city.mapY,
                identityColor = city.identityColor,
                isSelected = city.geonameId == selectedCityId
            )
        }
    }

    val activeSelectedCity = remember(selectedCityId, savedCities) {
        selectedCityId?.let { id ->
            savedCities.find { it.geonameId == id } ?: CityRepository.getAllCities().find { it.geonameId == id }
        }
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Persistent World Map anchored at the top
                ChronaWorldMapHeader(
                    markers = mapMarkers,
                    collapseFraction = 0f,
                    onMarkerClick = { markerCityId ->
                        val id = markerCityId.toLongOrNull()
                        if (id != null) {
                            selectedCityId = id
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Independently scrollable Saved Cities List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedCities, key = { it.geonameId }) { city ->
                        val currentCity by rememberUpdatedState(city)
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    val target = currentCity
                                    val originalIndex = CityRepository.deleteSavedCity(target.geonameId)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${target.cityName} removed",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            CityRepository.restoreSavedCity(target.geonameId, originalIndex)
                                        }
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        val deleteBgColor = Color(0xFFFEB4AA)
                        val deleteIconColor = Color(0xFF680004)

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                val alignment = when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(deleteBgColor)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete city",
                                        tint = deleteIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TimezoneCard(
                                city = city,
                                is24Hour = preferences.is24HourFormat,
                                referenceInstant = now,
                                onClick = {
                                    selectedCityId = city.geonameId
                                }
                            )
                        }
                    }
                }
            }

            // Shared Add Place FAB matching Alarms screen FAB exactly
            ChronaFloatingActionButton(
                onClick = { showSearchSheet = true },
                contentDescription = "Add place",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = ChronaFabDefaults.endPadding, bottom = ChronaFabDefaults.bottomPadding)
            )

            // Undo Deletion Snackbar Host positioned above bottom bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.90f)
                    .padding(bottom = 116.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = extendedColors.surfaceHigh,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = data.visuals.message,
                                    color = extendedColors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                data.visuals.actionLabel?.let { actionLabel ->
                                    TextButton(
                                        onClick = { data.performAction() },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = actionLabel,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
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

    // Add Place Modal Bottom Sheet (~80% height)
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
