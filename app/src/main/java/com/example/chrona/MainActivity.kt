package com.example.chrona

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.chrona.core.alarm.AlarmAudioService
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.BedtimeRepository
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.feature.alarms.AlarmRingingActivity
import com.example.chrona.feature.timer.TimerManager
import com.example.chrona.navigation.ChronaNavHost
import com.example.chrona.ui.theme.ChronaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply cached manual theme before installing native splash screen
        val cachedThemeStr = getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
            .getString("cached_theme_mode", ThemeMode.SYSTEM.name)
        val cachedTheme = try {
            ThemeMode.valueOf(cachedThemeStr ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        PreferencesRepository.applyNightModeToSystem(this, cachedTheme)

        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val isDark = when (cachedTheme) {
            ThemeMode.AMOLED_DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> systemDark
        }

        if (isDark) {
            setTheme(R.style.Theme_Chrona_Starting_Dark)
        } else {
            setTheme(R.style.Theme_Chrona_Starting_Light)
        }

        // Install official AndroidX native splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent application repositories
        PreferencesRepository.init(this)
        AlarmRepository.init(this)
        CityRepository.init(this)
        TimerManager.init(this)
        BedtimeRepository.init(this)
        com.example.chrona.feature.stopwatch.StopwatchManager.init(this)

        handleIntent(intent)

        setContent {
            val preferences by PreferencesRepository.preferences.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (preferences.themeMode) {
                ThemeMode.AMOLED_DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }

            val customAccent = try {
                Color(android.graphics.Color.parseColor(preferences.accentColorHex))
            } catch (e: Exception) {
                null
            }

            ChronaTheme(
                darkTheme = isDarkTheme,
                customAccent = customAccent
            ) {
                ChronaNavHost()
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val ringingAlarmId = intent?.getStringExtra("RINGING_ALARM_ID")
        if (ringingAlarmId != null) {
            val ringIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmAudioService.EXTRA_ALARM_ID, ringingAlarmId)
            }
            startActivity(ringIntent)
        }

        val destStr = intent?.getStringExtra("OPEN_DESTINATION")
        if (destStr != null) {
            try {
                val dest = com.example.chrona.core.ui.components.NavDestination.valueOf(destStr)
                com.example.chrona.navigation.NavigationDispatcher.navigateTo(dest)
            } catch (_: Exception) {}
        }
    }
}
