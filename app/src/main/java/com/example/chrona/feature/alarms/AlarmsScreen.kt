package com.example.chrona.feature.alarms

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Public
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
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.City
import com.example.chrona.core.timezone.TimezoneEngine
import com.example.chrona.core.ui.components.ChronaFabDefaults
import com.example.chrona.core.ui.components.ChronaFloatingActionButton
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    initialWorldAlarmCity: City? = null,
    onClearInitialCity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = ChronaThemeTokens.extended
    val isDark = ChronaThemeTokens.isDark
    val alarms by AlarmRepository.alarms.collectAsState()
    val preferences by PreferencesRepository.preferences.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showCreateSheet by remember { mutableStateOf(initialWorldAlarmCity != null) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var timePickerAlarm by remember { mutableStateOf<Alarm?>(null) }

    LaunchedEffect(initialWorldAlarmCity) {
        if (initialWorldAlarmCity != null) {
            showCreateSheet = true
        }
    }

    Scaffold(
        topBar = {
            ChronaTopBar(
                title = "Alarms",
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
            if (alarms.isEmpty()) {
                // Empty State (alarms_empty.svg)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(extendedColors.surfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Alarm,
                                contentDescription = null,
                                tint = extendedColors.textMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "No alarms scheduled",
                            color = extendedColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Create local or cross-timezone alarms with automatic daylight saving calculations.",
                            color = extendedColors.textSecondary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                items(alarms, key = { it.id }) { alarm ->
                    val currentAlarm by rememberUpdatedState(alarm)
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                val deletedAlarm = currentAlarm
                                AlarmRepository.deleteAlarm(context, deletedAlarm.id)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Alarm deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        AlarmRepository.saveAlarm(context, deletedAlarm)
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
                                    contentDescription = "Delete alarm",
                                    tint = deleteIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AlarmCard(
                            alarm = alarm,
                            is24Hour = preferences.is24HourFormat,
                            onTimeClick = {
                                timePickerAlarm = alarm
                            },
                            onCardClick = {
                                editingAlarm = alarm
                                showCreateSheet = true
                            },
                            onToggle = {
                                val willBeEnabled = !alarm.isEnabled
                                AlarmRepository.toggleAlarm(context, alarm.id)
                                if (willBeEnabled) {
                                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                message = "Exact alarm permission required for precision timing",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        val now = Instant.now()
                                        val nextTrigger = TimezoneEngine.calculateNextTriggerInstant(alarm.copy(isEnabled = true))
                                        val diff = Duration.between(now, nextTrigger)
                                        val totalMinutes = diff.toMinutes().coerceAtLeast(1)
                                        val hours = totalMinutes / 60
                                        val minutes = totalMinutes % 60
                                        val durationStr = when {
                                            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                                            hours > 0 -> "${hours}h"
                                            else -> "${minutes}m"
                                        }
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                message = "Alarm will ring in $durationStr",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

            // 2. FLOATING ACTION BUTTON: Returned to original approved position
            ChronaFloatingActionButton(
                onClick = {
                    editingAlarm = null
                    showCreateSheet = true
                },
                contentDescription = "Add alarm",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = ChronaFabDefaults.endPadding, bottom = ChronaFabDefaults.bottomPadding)
            )

            // 3. ANCHORED MESSAGE BANNER: Directly above the plus button, 90% content width
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

    // RADIAL TIME PICKER DIALOG (When user taps alarm time)
    val pickerAlarm = timePickerAlarm
    if (pickerAlarm != null) {
        ChronaRadialTimePickerDialog(
            initialHour = pickerAlarm.hour,
            initialMinute = pickerAlarm.minute,
            is24Hour = preferences.is24HourFormat,
            onTimeSelected = { selectedH, selectedM ->
                val updatedAlarm = pickerAlarm.copy(hour = selectedH, minute = selectedM)
                AlarmRepository.saveAlarm(context, updatedAlarm)
                timePickerAlarm = null
            },
            onDismiss = { timePickerAlarm = null }
        )
    }

    // 70% EXPANDABLE BOTTOM SHEET FOR BOTH CREATING AND EDITING ALARMS
    if (showCreateSheet) {
        CreateAlarmSheet(
            initialCity = if (editingAlarm == null) initialWorldAlarmCity else null,
            editingAlarm = editingAlarm,
            onDismiss = {
                showCreateSheet = false
                editingAlarm = null
                onClearInitialCity()
            },
            onAlarmSaved = { savedAlarm ->
                showCreateSheet = false
                editingAlarm = null
                onClearInitialCity()
                coroutineScope.launch {
                    val nextTrigger = TimezoneEngine.calculateNextTriggerInstant(savedAlarm)
                    val msg = if (nextTrigger != null) {
                        val dur = Duration.between(Instant.now(), nextTrigger)
                        val h = dur.toHours()
                        val m = dur.toMinutes() % 60
                        "Alarm will ring in ${h}h ${m}m"
                    } else {
                        "Alarm saved"
                    }
                    snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                }
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    is24Hour: Boolean,
    onTimeClick: () -> Unit,
    onCardClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val preview = remember(alarm, is24Hour) {
        TimezoneEngine.previewLocalTime(alarm, is24Hour = is24Hour)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.cardBackground,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 4dp vertical identity strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (alarm.isEnabled) alarm.identityColor else extendedColors.outline)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badge if cross-timezone
                    if (alarm.isCrossTimezone) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (alarm.isEnabled) alarm.identityColor.copy(alpha = 0.2f) else extendedColors.surfaceHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = null,
                                    tint = if (alarm.isEnabled) alarm.identityColor else extendedColors.textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${alarm.targetCityName.uppercase()} TIME",
                                    color = if (alarm.isEnabled) alarm.identityColor else extendedColors.textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Big Time (Clickable directly opens radial picker)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onTimeClick() }
                            .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = alarm.formattedTargetTime(is24Hour),
                            color = if (alarm.isEnabled) extendedColors.textPrimary else extendedColors.textMuted,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )
                        if (!is24Hour) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = alarm.targetAmPm(),
                                color = if (alarm.isEnabled) extendedColors.textSecondary else extendedColors.textMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Label and repeat
                    Text(
                        text = "${alarm.label} · ${alarm.repeatSummary()}",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp
                    )

                    // If Cross-timezone, show local conversion pill
                    if (alarm.isCrossTimezone && alarm.isEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = extendedColors.surfaceHigh
                        ) {
                            Text(
                                text = "Rings locally at ${preview.deviceTime} ${preview.deviceAmPm} (${preview.dayOffsetDescription})",
                                color = extendedColors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Enable/Disable Switch (Clicking toggle only toggles alarm state)
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0B1320),
                        checkedTrackColor = alarm.identityColor
                    )
                )
            }
        }
    }
}
