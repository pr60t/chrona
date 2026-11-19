package com.example.chrona.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.City
import com.example.chrona.core.ui.components.ChronaBottomBar
import com.example.chrona.core.ui.components.NavDestination
import com.example.chrona.feature.alarms.AlarmsScreen
import com.example.chrona.feature.alarms.RingingAlarmScreen
import com.example.chrona.feature.bedtime.BedtimeScreen
import com.example.chrona.feature.onboarding.AboutDialog
import com.example.chrona.feature.onboarding.OnboardingDialog
import com.example.chrona.feature.screensaver.ScreensaverScreen
import com.example.chrona.feature.settings.SettingsScreen
import com.example.chrona.feature.stopwatch.StopwatchScreen
import com.example.chrona.feature.timer.TimerScreen
import com.example.chrona.feature.worldclock.WorldClockScreen

@Composable
fun ChronaNavHost(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences by PreferencesRepository.preferences.collectAsState()
    val ringingAlarm by AlarmRepository.ringingAlarm.collectAsState()

    var currentDestination by rememberSaveable { mutableStateOf(NavDestination.WORLD_CLOCK) }
    var initialWorldAlarmCity by remember { mutableStateOf<City?>(null) }

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showScreensaver by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by rememberSaveable { mutableStateOf(!preferences.hasCompletedOnboarding) }

    val saveableStateHolder = rememberSaveableStateHolder()

    // If a ringing alarm is active, show the full-screen ringing screen
    if (ringingAlarm != null) {
        RingingAlarmScreen(
            alarm = ringingAlarm!!,
            onDismiss = {
                com.example.chrona.core.alarm.AlarmReceiver.stopAlarmPlayback()
                AlarmRepository.setRingingAlarm(null)
            },
            onSnooze = {
                com.example.chrona.core.alarm.AlarmReceiver.stopAlarmPlayback()
                val alarmId = ringingAlarm!!.id
                AlarmRepository.setRingingAlarm(null)
                AlarmRepository.snoozeAlarm(context, alarmId)
            }
        )
        return
    }

    // Screensaver full-screen mode
    if (showScreensaver) {
        ScreensaverScreen(onDismiss = { showScreensaver = false })
        return
    }

    // Settings full-screen mode
    if (showSettings) {
        SettingsScreen(onNavigateBack = { showSettings = false })
        return
    }

    Scaffold(
        bottomBar = {
            ChronaBottomBar(
                currentDestination = currentDestination,
                onNavigate = { destination ->
                    currentDestination = destination
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            saveableStateHolder.SaveableStateProvider(currentDestination) {
                when (currentDestination) {
                    NavDestination.ALARMS -> {
                        AlarmsScreen(
                            onNavigateToSettings = { showSettings = true },
                            onNavigateToScreensaver = { showScreensaver = true },
                            onNavigateToAbout = { showAbout = true },
                            initialWorldAlarmCity = initialWorldAlarmCity,
                            onClearInitialCity = { initialWorldAlarmCity = null }
                        )
                    }

                    NavDestination.WORLD_CLOCK -> {
                        WorldClockScreen(
                            onNavigateToSettings = { showSettings = true },
                            onNavigateToScreensaver = { showScreensaver = true },
                            onNavigateToAbout = { showAbout = true },
                            onSetAlarmForCity = { city ->
                                initialWorldAlarmCity = city
                                currentDestination = NavDestination.ALARMS
                            }
                        )
                    }

                    NavDestination.TIMER -> {
                        TimerScreen(
                            onNavigateToSettings = { showSettings = true },
                            onNavigateToScreensaver = { showScreensaver = true },
                            onNavigateToAbout = { showAbout = true }
                        )
                    }

                    NavDestination.STOPWATCH -> {
                        StopwatchScreen(
                            onNavigateToSettings = { showSettings = true },
                            onNavigateToScreensaver = { showScreensaver = true },
                            onNavigateToAbout = { showAbout = true }
                        )
                    }

                    NavDestination.BEDTIME -> {
                        BedtimeScreen(
                            onNavigateToSettings = { showSettings = true },
                            onNavigateToScreensaver = { showScreensaver = true },
                            onNavigateToAbout = { showAbout = true }
                        )
                    }
                }
            }
        }
    }

    // Onboarding Dialog
    if (showOnboarding) {
        OnboardingDialog(
            onDismiss = {
                showOnboarding = false
                PreferencesRepository.completeOnboarding(context)
            }
        )
    }

    // About Dialog
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}
