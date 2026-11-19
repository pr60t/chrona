package com.example.chrona.feature.bedtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.BedtimeRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS

@Composable
fun BedtimeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val schedule by BedtimeRepository.schedule.collectAsState()
    val preferences by PreferencesRepository.preferences.collectAsState()

    Scaffold(
        topBar = {
            ChronaTopBar(
                title = "Bedtime",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToScreensaver = onNavigateToScreensaver,
                onNavigateToAbout = onNavigateToAbout
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Schedule Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Schedule",
                                color = extendedColors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${schedule.formattedSleepDuration()} of sleep planned",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = schedule.isEnabled,
                            onCheckedChange = { BedtimeRepository.toggleEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B1320),
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bedtime & Wakeup row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bedtime
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = extendedColors.surfaceHigh,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Bedtime,
                                        contentDescription = null,
                                        tint = Color(0xFFB8C7F2),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Bedtime",
                                        color = extendedColors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = schedule.formattedBedtime(preferences.is24HourFormat),
                                    color = extendedColors.textPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                )
                            }
                        }

                        // Wake up
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = extendedColors.surfaceHigh,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFFFCD34D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Wake up",
                                        color = extendedColors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = schedule.formattedWakeTime(preferences.is24HourFormat),
                                    color = extendedColors.textPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Repeat Days
                    Text(
                        text = "Days active",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7)
                        days.forEach { (name, dayVal) ->
                            val isSelected = schedule.daysOfWeek.contains(dayVal)
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val newDays = if (isSelected) schedule.daysOfWeek - dayVal else schedule.daysOfWeek + dayVal
                                        BedtimeRepository.updateSchedule(schedule.copy(daysOfWeek = newDays))
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color(0xFF0B1320) else extendedColors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bedtime Reminder Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(extendedColors.surfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bedtime reminder",
                            color = extendedColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${schedule.reminderMinutesBefore} minutes before bedtime",
                            color = extendedColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Sleep Hygiene Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.surfaceLow
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sleep consistency",
                        color = extendedColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Going to bed and waking up at consistent hours stabilizes circadian rhythms and improves sleep quality across timezones.",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
