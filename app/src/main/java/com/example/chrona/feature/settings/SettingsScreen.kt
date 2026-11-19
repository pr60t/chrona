package com.example.chrona.feature.settings

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
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.ui.theme.ChronaThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = ChronaThemeTokens.extended
    val preferences by PreferencesRepository.preferences.collectAsState()

    var canExactAlarm by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    var areNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    // Re-check permissions on lifecycle ON_RESUME (e.g. when returning from system Settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canExactAlarm = AlarmScheduler.canScheduleExactAlarms(context)
                areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val accentOptions = listOf(
        "#93A8C7" to "Periwinkle",
        "#A7E8D8" to "Mint Teal",
        "#E7A19A" to "Soft Coral",
        "#D4B8F0" to "Lavender",
        "#F2DF9B" to "Soft Gold",
        "#A3C7DE" to "Pale Blue"
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        accentOptions.forEach { (hex, name) ->
                            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                            val isSelected = preferences.accentColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) extendedColors.textPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { PreferencesRepository.setAccentColor(context, hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = Color(0xFF0B1320),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
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
                    // 24 Hour format toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "24-hour format",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Use 13:00 instead of 1:00 PM",
                                color = extendedColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = preferences.is24HourFormat,
                            onCheckedChange = { PreferencesRepository.set24HourFormat(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B1320),
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    HorizontalDivider(color = extendedColors.divider)

                    // Show city images toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Show city images",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Display visual cards in World Clock",
                                color = extendedColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = preferences.showCityImages,
                            onCheckedChange = { PreferencesRepository.setShowCityImages(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B1320),
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
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
                }
            }
        }
    }
}
