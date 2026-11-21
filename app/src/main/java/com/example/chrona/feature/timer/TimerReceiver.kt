package com.example.chrona.feature.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val timerId = intent?.getStringExtra(TimerNotificationManager.EXTRA_TIMER_ID) ?: return

        when (intent.action) {
            TimerNotificationManager.ACTION_STOP -> {
                TimerManager.togglePlayPause(timerId)
                TimerNotificationManager.cancelNotification(context, timerId)
            }
            TimerNotificationManager.ACTION_ADD_MINUTE -> {
                TimerManager.addOneMinute(timerId)
                val updatedTimer = TimerManager.timers.value.find { it.id == timerId }
                if (updatedTimer != null) {
                    TimerNotificationManager.updateNotification(context, updatedTimer)
                }
            }
        }
    }
}
