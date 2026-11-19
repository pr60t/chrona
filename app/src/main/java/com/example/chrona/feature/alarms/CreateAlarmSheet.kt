package com.example.chrona.feature.alarms

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.City
import com.example.chrona.core.model.DstPolicy
import com.example.chrona.core.timezone.TimezoneEngine
import com.example.chrona.feature.worldclock.TimezoneSearchSheet
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmSheet(
    modifier: Modifier = Modifier,
    initialCity: City? = null,
    editingAlarm: Alarm? = null,
    onDismiss: () -> Unit,
    onAlarmSaved: (Alarm) -> Unit
) {
    val context = LocalContext.current
    val extendedColors = ChronaThemeTokens.extended
    val isDark = ChronaThemeTokens.isDark
    val preferences by PreferencesRepository.preferences.collectAsState()
    val is24Hour = preferences.is24HourFormat

    val editCity = editingAlarm?.let { alarm ->
        CityRepository.getAllCities().find { it.zoneIdString == alarm.targetZoneIdString }
    }

    var isWorldAlarm by remember {
        mutableStateOf(editingAlarm?.isCrossTimezone ?: (initialCity != null))
    }
    var selectedCity by remember {
        mutableStateOf(editCity ?: initialCity ?: CityRepository.getAllCities().first { it.cityName == "Tokyo" })
    }
    var showCitySearch by remember { mutableStateOf(false) }

    var hour by remember { mutableIntStateOf(editingAlarm?.hour ?: if (initialCity != null) 9 else 7) }
    var minute by remember { mutableIntStateOf(editingAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(editingAlarm?.label ?: if (initialCity != null) "${selectedCity.cityName} Sync" else "Morning Wake-Up") }
    var daysOfWeek by remember { mutableStateOf(editingAlarm?.daysOfWeek ?: setOf(1, 2, 3, 4, 5)) } // Mon-Fri
    var vibrate by remember { mutableStateOf(editingAlarm?.vibrate ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(editingAlarm?.snoozeDurationMinutes ?: 10) }
    var gradualVolumeRamp by remember { mutableStateOf(editingAlarm?.gradualVolumeRamp ?: true) }
    var soundUri by remember { mutableStateOf(editingAlarm?.soundUri) }
    var soundTitle by remember {
        mutableStateOf(
            if (editingAlarm?.soundUri != null) {
                try {
                    val r = RingtoneManager.getRingtone(context, Uri.parse(editingAlarm.soundUri))
                    r?.getTitle(context) ?: "Custom alarm sound"
                } catch (e: Exception) {
                    "Custom sound"
                }
            } else {
                "Pixel Chimes"
            }
        )
    }

    var dstPolicy by remember { mutableStateOf(editingAlarm?.dstPolicy ?: DstPolicy.NEXT_VALID_TIME) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (pickedUri != null) {
                soundUri = pickedUri.toString()
                val ringtone = RingtoneManager.getRingtone(context, pickedUri)
                soundTitle = ringtone?.getTitle(context) ?: "Custom alarm sound"
            }
        }
    }

    val targetZoneId = remember(isWorldAlarm, selectedCity) {
        if (isWorldAlarm) selectedCity.zoneId else ZoneId.systemDefault()
    }

    // DST Check
    val dstStatus = remember(targetZoneId, hour, minute) {
        TimezoneEngine.checkDstStatus(targetZoneId, LocalDate.now(), LocalTime.of(hour, minute))
    }

    // Live Local Preview calculation
    val dummyAlarm = remember(isWorldAlarm, selectedCity, hour, minute, daysOfWeek, dstPolicy) {
        Alarm(
            hour = hour,
            minute = minute,
            targetZoneIdString = if (isWorldAlarm) selectedCity.zoneIdString else "SYSTEM",
            targetCityName = if (isWorldAlarm) selectedCity.cityName else "Local Time",
            targetCountryName = if (isWorldAlarm) selectedCity.countryName else "",
            identityColorHex = if (isWorldAlarm) selectedCity.identityColorHex else "#93A8C7",
            daysOfWeek = daysOfWeek,
            dstPolicy = dstPolicy
        )
    }

    val preview = remember(dummyAlarm, is24Hour) {
        TimezoneEngine.previewLocalTime(dummyAlarm, is24Hour = is24Hour)
    }

    // Calculate time until alarm rings
    val nextInstant = remember(dummyAlarm) {
        TimezoneEngine.calculateNextTriggerInstant(dummyAlarm)
    }
    val timeUntilText = remember(nextInstant) {
        if (nextInstant != null) {
            val dur = Duration.between(Instant.now(), nextInstant)
            val h = dur.toHours()
            val m = dur.toMinutes() % 60
            "In ${h}h ${m}m"
        } else {
            "Tomorrow"
        }
    }

    fun saveAlarmAndDismiss() {
        val alarmToSave = Alarm(
            id = editingAlarm?.id ?: java.util.UUID.randomUUID().toString(),
            label = label.ifBlank { if (isWorldAlarm) "${selectedCity.cityName} Alarm" else "Alarm" },
            hour = hour,
            minute = minute,
            targetZoneIdString = if (isWorldAlarm) selectedCity.zoneIdString else "SYSTEM",
            targetCityName = if (isWorldAlarm) selectedCity.cityName else "Local Time",
            targetCountryName = if (isWorldAlarm) selectedCity.countryName else "",
            identityColorHex = if (isWorldAlarm) selectedCity.identityColorHex else "#93A8C7",
            daysOfWeek = daysOfWeek,
            isEnabled = true,
            vibrate = vibrate,
            soundUri = soundUri,
            snoozeDurationMinutes = snoozeMinutes,
            gradualVolumeRamp = gradualVolumeRamp,
            dstPolicy = dstPolicy
        )

        AlarmRepository.saveAlarm(context, alarmToSave)
        onAlarmSaved(alarmToSave)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingAlarm != null) {
                            if (isWorldAlarm) "Edit world alarm" else "Edit alarm"
                        } else {
                            if (isWorldAlarm) "World alarm" else "New local alarm"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.textPrimary
                    )
                }

                TextButton(onClick = { saveAlarmAndDismiss() }) {
                    Text(
                        text = "Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TYPE TOGGLE TABS (Local Alarm vs World Alarm)
                if (editingAlarm == null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(100.dp),
                        color = extendedColors.surfaceLow
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { isWorldAlarm = false },
                                shape = RoundedCornerShape(100.dp),
                                color = if (!isWorldAlarm) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Local Alarm",
                                        fontWeight = if (!isWorldAlarm) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isWorldAlarm) MaterialTheme.colorScheme.primary else extendedColors.textSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { isWorldAlarm = true },
                                shape = RoundedCornerShape(100.dp),
                                color = if (isWorldAlarm) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "World Alarm",
                                        fontWeight = if (isWorldAlarm) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isWorldAlarm) MaterialTheme.colorScheme.primary else extendedColors.textSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // IF WORLD ALARM: TARGET CITY HEADER CARD
                if (isWorldAlarm) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = extendedColors.cardBackground,
                        border = BorderStroke(1.2.dp, selectedCity.identityColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(selectedCity.identityColor)
                                )
                                Column {
                                    Text(
                                        text = "${selectedCity.cityName}, ${selectedCity.countryName}",
                                        color = extendedColors.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${selectedCity.zoneIdString} · ${selectedCity.formattedOffset()}",
                                        color = selectedCity.identityColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showCitySearch = true }
                            ) {
                                Text(
                                    text = "Change",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // TIME DISPLAY CARD (Matches create_local_alarm.svg & create_world_alarm.svg)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showTimePickerDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.2.dp, if (isWorldAlarm) selectedCity.identityColor.copy(alpha = 0.4f) else extendedColors.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isWorldAlarm) {
                                "DESTINATION TIME · ${selectedCity.cityName.uppercase()} (${selectedCity.formattedOffset()})"
                            } else {
                                "LOCAL ALARM TIME · ${ZoneId.systemDefault().id.split("/").last().replace("_", " ").uppercase()}"
                            },
                            color = if (isWorldAlarm) selectedCity.identityColor else MaterialTheme.colorScheme.primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val displayHour = if (is24Hour) hour else {
                            when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Digital Digits
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Hour
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF2A384D) else Color(0xFFDCE6F5),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.size(width = 96.dp, height = 66.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", displayHour),
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) Color(0xFFDCE6F7) else Color(0xFF1E3352),
                                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                        )
                                    }
                                }

                                Text(
                                    text = ":",
                                    fontSize = 32.sp,
                                    color = extendedColors.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // Minute
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = extendedColors.surfaceHigh,
                                    modifier = Modifier.size(width = 96.dp, height = 66.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", minute),
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = extendedColors.textPrimary,
                                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                        )
                                    }
                                }

                                if (!is24Hour) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (hour >= 12) "PM" else "AM",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Time-until countdown badge
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = timeUntilText,
                                    color = extendedColors.success,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = preview.dayOffsetDescription,
                                    color = extendedColors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                // IF WORLD ALARM: DUAL COMPARISON BOTTOM CARD
                if (isWorldAlarm) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = extendedColors.surfaceLow,
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFF2DF9B))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "YOUR PHONE WILL RING AT · LOCAL TIME",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${preview.deviceTime} ${preview.deviceAmPm}",
                                        color = extendedColors.textPrimary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${preview.dayOffsetDescription})",
                                        color = extendedColors.success,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // DST GAP / OVERLAP WARNING & POLICY RESOLUTION
                    if (dstStatus.isGap || dstStatus.isOverlap) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = if (dstStatus.isGap) extendedColors.warning.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (dstStatus.isGap) extendedColors.warning else MaterialTheme.colorScheme.primary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = if (dstStatus.isGap) extendedColors.warning else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (dstStatus.isGap) "Daylight Saving Gap Detected" else "Daylight Saving Fallback Overlap",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = extendedColors.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dstStatus.message ?: "Daylight saving transition affects this time",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Resolution Policy:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (dstStatus.isGap) {
                                        FilterChip(
                                            selected = dstPolicy == DstPolicy.NEXT_VALID_TIME,
                                            onClick = { dstPolicy = DstPolicy.NEXT_VALID_TIME },
                                            label = { Text("Next Valid Time", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = dstPolicy == DstPolicy.SKIP_OCCURRENCE,
                                            onClick = { dstPolicy = DstPolicy.SKIP_OCCURRENCE },
                                            label = { Text("Skip", fontSize = 11.sp) }
                                        )
                                    } else {
                                        FilterChip(
                                            selected = dstPolicy == DstPolicy.FIRST_OCCURRENCE,
                                            onClick = { dstPolicy = DstPolicy.FIRST_OCCURRENCE },
                                            label = { Text("1st Time", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = dstPolicy == DstPolicy.SECOND_OCCURRENCE,
                                            onClick = { dstPolicy = DstPolicy.SECOND_OCCURRENCE },
                                            label = { Text("2nd Time", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // LABEL CARD
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showLabelDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.divider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Label,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = label.ifBlank { "Add label" },
                                color = extendedColors.textPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "Label",
                            color = extendedColors.textSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // RECURRENCE CARD (Weekdays selector S M T W T F S)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.divider)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repeat",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.textPrimary
                            )
                            Text(
                                text = when {
                                    daysOfWeek.isEmpty() -> "Once"
                                    daysOfWeek.size == 7 -> "Every day"
                                    daysOfWeek == setOf(1, 2, 3, 4, 5) -> "Weekdays"
                                    daysOfWeek == setOf(6, 7) -> "Weekends"
                                    else -> "Custom"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // S M T W T F S Circles
                        val dayMap = listOf(
                            Pair("S", 7), // Sunday
                            Pair("M", 1), // Monday
                            Pair("T", 2),
                            Pair("W", 3),
                            Pair("T", 4),
                            Pair("F", 5),
                            Pair("S", 6)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayMap.forEach { (name, dayNum) ->
                                val isSelected = daysOfWeek.contains(dayNum)
                                val circleColor = if (isSelected) {
                                    if (isWorldAlarm) selectedCity.identityColor else MaterialTheme.colorScheme.primary
                                } else {
                                    extendedColors.surfaceHigh
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            daysOfWeek = if (isSelected) {
                                                daysOfWeek - dayNum
                                            } else {
                                                daysOfWeek + dayNum
                                            }
                                        },
                                    shape = CircleShape,
                                    color = circleColor,
                                    border = if (!isSelected) BorderStroke(1.dp, extendedColors.outline) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) {
                                                Color(0xFF0B1320)
                                            } else {
                                                extendedColors.textSecondary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AUDIO SETTINGS CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = extendedColors.cardBackground,
                    border = BorderStroke(1.dp, extendedColors.divider)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        // 1. Alarm Sound Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                        if (soundUri != null) {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(soundUri))
                                        }
                                    }
                                    ringtonePickerLauncher.launch(intent)
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Alarm sound", fontSize = 14.sp, color = extendedColors.textPrimary)
                            }
                            Text(
                                text = soundTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(color = extendedColors.divider)

                        // 2. Vibration Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Vibration", fontSize = 14.sp, color = extendedColors.textPrimary)
                            }
                            Switch(
                                checked = vibrate,
                                onCheckedChange = { vibrate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0B1320),
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        HorizontalDivider(color = extendedColors.divider)

                        // 3. Gradual Volume Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text("Gradual volume", fontSize = 14.sp, color = extendedColors.textPrimary)
                                    Text(
                                        "Ramp volume smoothly over 30 seconds",
                                        fontSize = 11.5.sp,
                                        color = extendedColors.textSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = gradualVolumeRamp,
                                onCheckedChange = { gradualVolumeRamp = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0B1320),
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        HorizontalDivider(color = extendedColors.divider)

                        // 4. Snooze Duration Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Snooze,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Snooze duration", fontSize = 14.sp, color = extendedColors.textPrimary)
                            }

                            // Cycle snooze minutes capsule
                            val snoozeOptions = listOf(5, 10, 15, 20, 30)
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .clickable {
                                        val idx = snoozeOptions.indexOf(snoozeMinutes)
                                        snoozeMinutes = snoozeOptions[(idx + 1) % snoozeOptions.size]
                                    }
                            ) {
                                Text(
                                    text = "$snoozeMinutes min",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SAVE ALARM PRIMARY CAPSULE BUTTON (372x52 rx26)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { saveAlarmAndDismiss() },
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF0B1320),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isWorldAlarm) "Save world alarm" else "Save alarm",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1320)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // RADIAL TIME PICKER DIALOG (Matching batch 3 & 8)
    if (showTimePickerDialog) {
        ChronaRadialTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = is24Hour,
            onTimeSelected = { selectedH, selectedM ->
                hour = selectedH
                minute = selectedM
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    // LABEL EDIT DIALOG
    if (showLabelDialog) {
        var tempLabel by remember { mutableStateOf(label) }
        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            title = { Text("Alarm label", color = extendedColors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = tempLabel,
                    onValueChange = { tempLabel = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = extendedColors.textPrimary,
                        unfocusedTextColor = extendedColors.textPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    label = tempLabel
                    showLabelDialog = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLabelDialog = false }) {
                    Text("Cancel", color = extendedColors.textSecondary)
                }
            }
        )
    }

    // TIMEZONE SEARCH SHEET
    if (showCitySearch) {
        TimezoneSearchSheet(
            onDismiss = { showCitySearch = false },
            onCitySelected = { city ->
                selectedCity = city
                showCitySearch = false
            }
        )
    }
}
