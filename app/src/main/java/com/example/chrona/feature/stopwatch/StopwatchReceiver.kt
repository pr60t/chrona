package com.example.chrona.feature.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopwatchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            StopwatchNotificationManager.ACTION_PAUSE -> {
                StopwatchManager.pause()
                StopwatchNotificationManager.updateNotification(context)
            }
            StopwatchNotificationManager.ACTION_RESUME -> {
                StopwatchManager.start()
                StopwatchNotificationManager.updateNotification(context)
            }
            StopwatchNotificationManager.ACTION_LAP -> {
                StopwatchManager.recordLap()
                StopwatchNotificationManager.updateNotification(context)
            }
            StopwatchNotificationManager.ACTION_RESET -> {
                StopwatchManager.reset()
                StopwatchNotificationManager.updateNotification(context)
            }
        }
    }
}
