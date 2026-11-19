package com.example.chrona.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.chrona.core.data.AlarmRepository

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_DISMISS = "com.example.chrona.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.example.chrona.ACTION_SNOOZE_ALARM"

        fun stopAlarmPlayback() {
            // Kept for backward compatibility with any direct caller
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        Log.d(TAG, "Alarm broadcast received: $alarmId with action ${intent.action}")

        when (intent.action) {
            ACTION_DISMISS -> {
                AlarmAudioService.dismissAlarm(context, alarmId)
            }
            ACTION_SNOOZE -> {
                AlarmAudioService.snoozeAlarm(context, alarmId)
            }
            else -> {
                val alarm = AlarmRepository.getAlarmById(context, alarmId)
                // If recurring alarm, calculate and schedule the next occurrence
                if (alarm != null && alarm.daysOfWeek.isNotEmpty()) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }

                // Hand over playback and ongoing notification to the foreground service
                AlarmAudioService.startAlarm(context, alarmId)
            }
        }
    }
}
