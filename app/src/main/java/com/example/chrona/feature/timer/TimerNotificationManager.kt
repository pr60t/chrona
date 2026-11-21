package com.example.chrona.feature.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chrona.MainActivity
import com.example.chrona.R
import com.example.chrona.core.model.TimerItem

object TimerNotificationManager {
    const val CHANNEL_ID = "chrona_timer_ongoing"
    const val NOTIFICATION_BASE_ID = 3000

    const val ACTION_STOP = "com.example.chrona.TIMER_STOP"
    const val ACTION_ADD_MINUTE = "com.example.chrona.TIMER_ADD_MINUTE"
    const val EXTRA_TIMER_ID = "EXTRA_TIMER_ID"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Chrona Running Timers",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Displays live countdown and controls for active timers"
                    setShowBadge(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun getNotificationId(timerId: String): Int {
        return NOTIFICATION_BASE_ID + kotlin.math.abs(timerId.hashCode() % 10000)
    }

    fun updateNotification(context: Context, timer: TimerItem) {
        createChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = getNotificationId(timer.id)

        // Only show ongoing countdown notification while running with remaining time
        if (!timer.isRunning || timer.remainingMillis <= 0 || timer.isFinished) {
            nm.cancel(notifId)
            return
        }

        // Tap content opens Chrona on Timer tab
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_DESTINATION", "TIMER")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wallClockDeadline = System.currentTimeMillis() + timer.remainingMillis

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chrona_symbol)
            .setContentTitle(timer.label.ifBlank { "Timer" })
            .setContentText("Counting down")
            .setContentIntent(contentPendingIntent)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(wallClockDeadline)
            .setShowWhen(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Action 1: Stop
        val stopIntent = Intent(context, TimerReceiver::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_TIMER_ID, timer.id)
        }
        val stopPending = PendingIntent.getBroadcast(
            context,
            notifId * 10 + 1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            stopPending
        )

        // Action 2: +1 min
        val addMinIntent = Intent(context, TimerReceiver::class.java).apply {
            action = ACTION_ADD_MINUTE
            putExtra(EXTRA_TIMER_ID, timer.id)
        }
        val addMinPending = PendingIntent.getBroadcast(
            context,
            notifId * 10 + 2,
            addMinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            android.R.drawable.ic_input_add,
            "+1 min",
            addMinPending
        )

        nm.notify(notifId, builder.build())
    }

    fun cancelNotification(context: Context, timerId: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(getNotificationId(timerId))
    }
}
