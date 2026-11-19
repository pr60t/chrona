package com.example.chrona

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.chrona.core.alarm.AlarmAudioService
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.feature.alarms.AlarmRingingActivity
import com.example.chrona.feature.timer.TimerManager
import com.example.chrona.navigation.ChronaNavHost
import com.example.chrona.ui.theme.ChronaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent application repositories
        PreferencesRepository.init(this)
        AlarmRepository.init(this)
        CityRepository.init(this)
        TimerManager.init(this)

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
    }
}
