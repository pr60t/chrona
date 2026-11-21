package com.example.chrona.feature.stopwatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.chrona.MainActivity
import com.example.chrona.R

object StopwatchNotificationManager {
    const val CHANNEL_ID = "chrona_stopwatch"
    const val NOTIFICATION_ID = 2001

    const val ACTION_PAUSE = "com.example.chrona.STOPWATCH_PAUSE"
    const val ACTION_RESUME = "com.example.chrona.STOPWATCH_RESUME"
    const val ACTION_LAP = "com.example.chrona.STOPWATCH_LAP"
    const val ACTION_RESET = "com.example.chrona.STOPWATCH_RESET"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Chrona Stopwatch",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Displays live elapsed time and stopwatch controls"
                    setShowBadge(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun updateNotification(context: Context) {
        createChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isRunning = StopwatchManager.isRunning.value
        val elapsed = StopwatchManager.getElapsedMillis()

        // If stopped and elapsed is 0, cancel notification
        if (!isRunning && elapsed == 0L) {
            nm.cancel(NOTIFICATION_ID)
            return
        }

        // Tap content opens Chrona on Stopwatch tab
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_DESTINATION", "STOPWATCH")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chrona_symbol)
            .setContentIntent(contentPendingIntent)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (isRunning) {
            // Running: Native live Chronometer
            val baseTime = SystemClock.elapsedRealtime() - elapsed
            val wallClockBase = System.currentTimeMillis() - elapsed

            builder.setContentTitle("Stopwatch")
                .setContentText("Stopwatch is running")
                .setUsesChronometer(true)
                .setChronometerCountDown(false)
                .setWhen(wallClockBase)
                .setShowWhen(true)
                .setOngoing(true)

            // Action 1: Pause
            val pauseIntent = Intent(context, StopwatchReceiver::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePending = PendingIntent.getBroadcast(
                context,
                101,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePending
            )

            // Action 2: Lap
            val lapIntent = Intent(context, StopwatchReceiver::class.java).apply {
                action = ACTION_LAP
            }
            val lapPending = PendingIntent.getBroadcast(
                context,
                102,
                lapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.drawable.ic_chrona_symbol,
                "Lap",
                lapPending
            )

            // Reset is omitted while running per native Android platform constraint
        } else {
            // Paused: Static frozen elapsed time
            val formatted = formatElapsed(elapsed)
            builder.setContentTitle("Stopwatch (Paused)")
                .setContentText("Elapsed: $formatted")
                .setUsesChronometer(false)
                .setShowWhen(false)
                .setOngoing(true)

            // Action 1: Resume
            val resumeIntent = Intent(context, StopwatchReceiver::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePending = PendingIntent.getBroadcast(
                context,
                103,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Resume",
                resumePending
            )

            // Action 2: Reset
            val resetIntent = Intent(context, StopwatchReceiver::class.java).apply {
                action = ACTION_RESET
            }
            val resetPending = PendingIntent.getBroadcast(
                context,
                104,
                resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_revert,
                "Reset",
                resetPending
            )
        }

        nm.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    private fun formatElapsed(millis: Long): String {
        val totalSecs = millis / 1000
        val m = totalSecs / 60
        val s = totalSecs % 60
        val cs = (millis % 1000) / 100
        return "%02d:%02d.%01d".format(m, s, cs)
    }
}
