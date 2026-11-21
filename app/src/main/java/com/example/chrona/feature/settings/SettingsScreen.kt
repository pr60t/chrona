package com.example.chrona.feature.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.chrona.core.alarm.AlarmScheduler
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.ClockStyle
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.core.model.TimeFormatPreference
import com.example.chrona.ui.theme.ChronaThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept system/hardware/gesture Back so it returns to World Clock without exiting the app
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val extendedColors = ChronaThemeTokens.extended
    val preferences by PreferencesRepository.preferences.collectAsState()

    fun checkFullScreenPermission(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
    }

    var canExactAlarm by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    var areNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var canFullScreen by remember { mutableStateOf(checkFullScreenPermission(context)) }

    // Re-check permissions on lifecycle ON_RESUME (e.g. when returning from system Settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canExactAlarm = AlarmScheduler.canScheduleExactAlarms(context)
                areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                canFullScreen = checkFullScreenPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    data class AccentChoice(
        val hex: String,
        val name: String,
        val isDefault: Boolean = false
    )

    val accentOptions = listOf(
        AccentChoice("#93A8C7", "Periwinkle"),
        AccentChoice("#A7E8D8", "Mint Teal"),
        AccentChoice("#E7A19A", "Soft Coral"),
        AccentChoice("#DCE2FA", "Pixel", isDefault = true),
        AccentChoice("#D4B8F0", "Lavender"),
        AccentChoice("#F2DF9B", "Soft Gold"),
        AccentChoice("#A3C7DE", "Pale Blue")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Section
            Text(
                text = "APPEARANCE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme mode",
                        color = extendedColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ThemeMode.AMOLED_DARK to "AMOLED Dark",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.SYSTEM to "System"
                        ).forEach { (mode, label) ->
                            val isSelected = preferences.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { PreferencesRepository.setThemeMode(context, mode) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Accent color",
                        color = extendedColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        accentOptions.forEach { choice ->
                            val color = try { Color(android.graphics.Color.parseColor(choice.hex)) } catch (e: Exception) { Color.Gray }
                            val isSelected = preferences.accentColorHex.equals(choice.hex, ignoreCase = true)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { PreferencesRepository.setAccentColor(context, choice.hex) }
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = if (isSelected) extendedColors.textPrimary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = choice.name,
                                            tint = Color(0xFF0B1320),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (choice.isDefault) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "DEFAULT",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(18.dp))
                                }
                            }
                        }
                    }

                    val currentAccent = accentOptions.find { it.hex.equals(preferences.accentColorHex, ignoreCase = true) }
                    Text(
                        text = if (currentAccent?.isDefault == true) "Selected: Pixel (Default Google Pixel theme)" else "Selected: ${currentAccent?.name ?: "Custom"}",
                        color = extendedColors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Clock & Display Section
            Text(
                text = "CLOCK & DISPLAY",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Time format selector (Follow System, 12-hour, 24-hour)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Time format",
                            color = extendedColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Display times using 12-hour, 24-hour, or device system format",
                            color = extendedColors.textSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                TimeFormatPreference.FOLLOW_SYSTEM to "Follow system",
                                TimeFormatPreference.TWELVE_HOUR to "12-hour",
                                TimeFormatPreference.TWENTY_FOUR_HOUR to "24-hour"
                            ).forEach { (pref, label) ->
                                val isSelected = preferences.timeFormatPreference == pref
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else extendedColors.surfaceHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(100.dp))
                                        .clickable { PreferencesRepository.setTimeFormatPreference(context, pref) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = extendedColors.divider)

                    // Date & time system settings row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date & time",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Manage your device's date, time and timezone.",
                                color = extendedColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    HorizontalDivider(color = extendedColors.divider)

                    // Vibration toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vibrate on alarms & timers",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp
                            )
                        }
                        Switch(
                            checked = preferences.vibrationEnabled,
                            onCheckedChange = { PreferencesRepository.setVibrationEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B1320),
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Alarm Preferences Section
            Text(
                text = "ALARM PREFERENCES",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Default snooze duration slider (1..30 min)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Default snooze duration",
                            color = extendedColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${preferences.defaultSnoozeDurationMinutes} min",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Default snooze time for newly created alarms",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = preferences.defaultSnoozeDurationMinutes.toFloat(),
                        onValueChange = {
                            PreferencesRepository.setDefaultSnoozeDuration(context, it.toInt())
                        },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    HorizontalDivider(color = extendedColors.divider, modifier = Modifier.padding(vertical = 8.dp))

                    // Alarm playback volume slider (0..100%)
                    var localVolume by remember(preferences.alarmVolumePercent) {
                        mutableFloatStateOf(preferences.alarmVolumePercent.toFloat())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alarm volume",
                            color = extendedColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${localVolume.toInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Application playback volume for Chrona alarms",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = localVolume,
                        onValueChange = { localVolume = it },
                        onValueChangeFinished = {
                            PreferencesRepository.setAlarmVolumePercent(context, localVolume.toInt())
                        },
                        valueRange = 0f..100f,
                        steps = 0,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = extendedColors.surfaceHigh
                        )
                    )

                    HorizontalDivider(color = extendedColors.divider, modifier = Modifier.padding(vertical = 8.dp))

                    // Alarm Clock Style (Digital vs Analog)
                    Text(
                        text = "Alarm clock style",
                        color = extendedColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Clock style presented when an alarm is actively ringing",
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ClockStyle.DIGITAL to "Digital",
                            ClockStyle.ANALOG to "Analog"
                        ).forEach { (style, label) ->
                            val isSelected = preferences.alarmClockStyle == style
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { PreferencesRepository.setAlarmClockStyle(context, style) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Permissions Section
            Text(
                text = "PERMISSIONS & RELIABILITY",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Exact Alarm Scheduling Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exact alarm scheduling",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (canExactAlarm) "Granted · Alarms will ring precisely on time" else "Permission required to guarantee exact alarm timing",
                                color = if (canExactAlarm) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                fontSize = 13.sp
                            )
                        }

                        if (!canExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Enable")
                            }
                        }
                    }

                    HorizontalDivider(color = extendedColors.divider, modifier = Modifier.padding(vertical = 12.dp))

                    // Notifications Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notifications",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (areNotificationsEnabled) "Enabled · You will receive timer & alarm banners" else "Notifications are turned off in system settings",
                                color = if (areNotificationsEnabled) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                fontSize = 13.sp
                            )
                        }

                        if (!areNotificationsEnabled) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Enable")
                            }
                        }
                    }

                    HorizontalDivider(color = extendedColors.divider, modifier = Modifier.padding(vertical = 12.dp))

                    // Full-screen Alarms Status (Android 14+ / API 34+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Full-screen alarms",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (canFullScreen) {
                                    "Allowed · Alarms wake the display and show full-screen on lock"
                                } else {
                                    "Permission off · Alarms will use heads-up notification fallback"
                                },
                                color = if (canFullScreen) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                fontSize = 13.sp
                            )
                        }

                        if (!canFullScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            TextButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {
                                            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(fallbackIntent)
                                        }
                                    } catch (_: Exception) {
                                        try {
                                            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(fallbackIntent)
                                        } catch (_: Exception) {}
                                    }
                                }
                            ) {
                                Text("Allow")
                            }
                        }
                    }
                }
            }
        }
    }
}
