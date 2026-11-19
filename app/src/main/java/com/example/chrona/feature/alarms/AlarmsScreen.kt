package com.example.chrona.feature.alarms

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Edit
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
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Instant

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
    val alarms by AlarmRepository.alarms.collectAsState()
    val preferences by PreferencesRepository.preferences.collectAsState()

    var showCreateSheet by remember { mutableStateOf(initialWorldAlarmCity != null) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var expandedAlarmId by remember { mutableStateOf<String?>(null) }

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
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
                    text = "New alarm",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            // Empty State (alarms_empty.svg)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is24Hour = preferences.is24HourFormat,
                        isExpanded = expandedAlarmId == alarm.id,
                        onToggle = { AlarmRepository.toggleAlarm(context, alarm.id) },
                        onEdit = {
                            editingAlarm = alarm
                            showCreateSheet = true
                        },
                        onDelete = { AlarmRepository.deleteAlarm(context, alarm.id) },
                        onClick = {
                            expandedAlarmId = if (expandedAlarmId == alarm.id) null else alarm.id
                        }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateAlarmSheet(
            initialCity = initialWorldAlarmCity,
            editingAlarm = editingAlarm,
            onDismiss = {
                showCreateSheet = false
                editingAlarm = null
                onClearInitialCity()
            },
            onAlarmSaved = {
                showCreateSheet = false
                editingAlarm = null
                onClearInitialCity()
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    is24Hour: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
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
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.cardBackground,
        tonalElevation = 1.dp
    ) {
        Column {
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

                        // Big Time
                        Row(
                            verticalAlignment = Alignment.Bottom
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

                    // Enable/Disable Switch
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

            // Expandable controls (e.g. Delete)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(color = extendedColors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onEdit,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }

                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFCA5A5))
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
