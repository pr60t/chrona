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
        const val ACTION_CANCEL_SNOOZE = "com.example.chrona.ACTION_CANCEL_SNOOZE"

        fun stopAlarmPlayback() {
            // Kept for backward compatibility with any direct caller
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val rawAlarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val originalAlarmId = AlarmScheduler.getOriginalAlarmId(rawAlarmId)
        Log.d(TAG, "Alarm broadcast received: $rawAlarmId (original: $originalAlarmId) with action ${intent.action}")

        when (intent.action) {
            ACTION_DISMISS -> {
                SnoozeNotificationManager.cancelSnoozeNotification(context, originalAlarmId)
                AlarmAudioService.dismissAlarm(context, originalAlarmId)
            }
            ACTION_SNOOZE -> {
                AlarmAudioService.snoozeAlarm(context, originalAlarmId)
            }
            ACTION_CANCEL_SNOOZE -> {
                Log.d(TAG, "Cancelling snooze for alarm $originalAlarmId from notification action")
                AlarmScheduler.cancelSnooze(context, originalAlarmId)
                SnoozeNotificationManager.cancelSnoozeNotification(context, originalAlarmId)
            }
            else -> {
                // An alarm has triggered: remove any active snooze countdown notification for this alarm
                SnoozeNotificationManager.cancelSnoozeNotification(context, originalAlarmId)

                val alarm = AlarmRepository.getAlarmById(context, originalAlarmId)
                // If recurring alarm and this was a scheduled regular trigger (not a snooze trigger), calculate and schedule next occurrence
                if (!rawAlarmId.startsWith("snooze_") && alarm != null && alarm.daysOfWeek.isNotEmpty()) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }

                // Hand over playback and ongoing notification to the foreground service
                AlarmAudioService.startAlarm(context, originalAlarmId)
            }
        }
    }
}
