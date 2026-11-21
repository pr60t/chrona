package com.example.chrona.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.database.CityCatalogueDatabase
import com.example.chrona.core.model.City
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.ui.theme.ChronaTheme
import com.example.chrona.ui.theme.ChronaThemeTokens
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WorldClockConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set result to CANCELED first so back button cancels configuration
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Initialize dependencies
        PreferencesRepository.init(this)
        CityRepository.init(this)

        val currentWidgetCities = WidgetUpdateHelper.getWidgetCities(this, appWidgetId)

        setContent {
            val preferences by PreferencesRepository.preferences.collectAsState()
            val isDarkTheme = when (preferences.themeMode) {
                ThemeMode.AMOLED_DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> true
            }

            val customAccent = try {
                Color(android.graphics.Color.parseColor(preferences.accentColorHex))
            } catch (_: Exception) {
                null
            }

            ChronaTheme(
                darkTheme = isDarkTheme,
                customAccent = customAccent
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ChronaThemeTokens.extended.cardBackground
                ) {
                    WorldClockConfigContent(
                        initialSelected = currentWidgetCities,
                        onSave = { selectedCities ->
                            val ids = selectedCities.map { it.geonameId }
                            WidgetUpdateHelper.saveWidgetCities(this, appWidgetId, ids)
                            WidgetUpdateHelper.updateAllWidgets(this)

                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldClockConfigContent(
    initialSelected: List<City>,
    onSave: (List<City>) -> Unit,
    onCancel: () -> Unit
) {
    val extendedColors = ChronaThemeTokens.extended
    val savedCities by CityRepository.savedCities.collectAsState()

    // Ensure initial cities include defaults if savedCities is loading
    val availableCities = remember(savedCities) {
        val list = savedCities.toMutableList()
        CityRepository.defaultCityIds.forEach { id ->
            if (!list.any { it.geonameId == id }) {
                initialSelected.find { it.geonameId == id }?.let { list.add(it) }
            }
        }
        list
    }

    var selectedList by remember {
        mutableStateOf(initialSelected.take(4))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configure World Clock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSave(selectedList) },
                        enabled = selectedList.isNotEmpty()
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedList.isNotEmpty()) MaterialTheme.colorScheme.primary else extendedColors.textMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = extendedColors.cardBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Live Preview Card
            Text(
                text = "WIDGET PREVIEW (2 × 2)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF202022),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D2E34))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WORLD CLOCK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF929AAA),
                            letterSpacing = 1.5.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(modifier = Modifier.size(5.dp).background(Color(0xFF7C8292), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(5.dp).background(Color(0xFF96A8E8), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(5.dp).background(Color(0xFFE8B872), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.size(5.dp).background(Color(0xFFE8B872), RoundedCornerShape(1.dp)))
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF2E2F34),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PreviewCell(city = selectedList.getOrNull(0), modifier = Modifier.weight(1f))
                        VerticalDivider(color = Color(0xFF2E2F34))
                        PreviewCell(city = selectedList.getOrNull(1), modifier = Modifier.weight(1f).padding(start = 8.dp))
                    }

                    HorizontalDivider(
                        color = Color(0xFF2E2F34),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PreviewCell(city = selectedList.getOrNull(2), modifier = Modifier.weight(1f))
                        VerticalDivider(color = Color(0xFF2E2F34))
                        PreviewCell(city = selectedList.getOrNull(3), modifier = Modifier.weight(1f).padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SELECT CITIES (${selectedList.size}/4)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCities, key = { it.geonameId }) { city ->
                    val isSelected = selectedList.any { it.geonameId == city.geonameId }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (isSelected) {
                                    if (selectedList.size > 1) {
                                        selectedList = selectedList.filter { it.geonameId != city.geonameId }
                                    }
                                } else {
                                    if (selectedList.size < 4) {
                                        selectedList = selectedList + city
                                    }
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else extendedColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = city.cityName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = extendedColors.textPrimary
                                )
                                Text(
                                    text = "${city.countryName} · ${city.zoneIdString}",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textMuted,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF1B1B1D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCell(city: City?, modifier: Modifier = Modifier) {
    if (city == null) {
        Box(modifier = modifier)
        return
    }

    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val timeStr = try {
        ZonedDateTime.now(city.zoneId).format(timeFormatter)
    } catch (_: Exception) {
        "--:--"
    }

    val pixelColor = when {
        city.isUtc -> Color(0xFF7C8292)
        city.isDaytime() == true -> Color(0xFFE8B872)
        else -> Color(0xFF96A8E8)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(pixelColor, RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = city.cityName.uppercase(Locale.US),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF929AAA),
                maxLines = 1
            )
        }
        Text(
            text = timeStr,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFFDFE2F3)
        )
    }
}
