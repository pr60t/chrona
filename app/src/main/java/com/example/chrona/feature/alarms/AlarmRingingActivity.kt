package com.example.chrona.feature.alarms

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.chrona.core.alarm.AlarmAudioService
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.model.Alarm
import com.example.chrona.ui.theme.ChronaTheme

class AlarmRingingActivity : ComponentActivity() {

    private var alarmId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure lock-screen and screen turn-on behavior
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Keep screen on while actively ringing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onBackPressedDispatcher.addCallback(this) {
            snoozeAndFinish()
        }

        alarmId = intent.getStringExtra(AlarmAudioService.EXTRA_ALARM_ID)
        val initialAlarm = alarmId?.let { AlarmRepository.getAlarmById(this, it) }
            ?: Alarm(hour = 8, minute = 0, label = "Alarm")

        setContent {
            val ringingAlarmState by AlarmRepository.ringingAlarm.collectAsState()
            val currentAlarm = ringingAlarmState ?: initialAlarm

            ChronaTheme(
                darkTheme = true // Ringing screen is always pitch-black AMOLED per specification
            ) {
                RingingAlarmScreen(
                    alarm = currentAlarm,
                    onDismiss = {
                        dismissAndFinish()
                    },
                    onSnooze = {
                        snoozeAndFinish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun dismissAndFinish() {
        alarmId?.let { id ->
            AlarmAudioService.dismissAlarm(this, id)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        finishAndRemoveTask()
    }

    private fun snoozeAndFinish() {
        alarmId?.let { id ->
            AlarmAudioService.snoozeAlarm(this, id)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }
}
