package com.example.chrona.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chrona.R
import com.example.chrona.core.model.Alarm

object SnoozeNotificationManager {
    const val SNOOZE_CHANNEL_ID = "chrona_snooze_channel_v1"
    private const val NOTIFICATION_ID_BASE = 8000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel(
                SNOOZE_CHANNEL_ID,
                "Snoozed Alarms",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for snoozed alarms"
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showSnoozeNotification(context: Context, alarm: Alarm, snoozeMinutes: Int) {
        createChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val notifId = NOTIFICATION_ID_BASE + Math.abs(alarm.id.hashCode() % 1000)
        val title = "Alarm snoozed"
        val contentText = "${alarm.label} will ring again in $snoozeMinutes minutes."

        // Dismiss action intent (broadcast to AlarmReceiver)
        val cancelIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_CANCEL_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PUBLIC version: shown when device is locked. Contains status but NO dismiss action!
        val publicNotification = NotificationCompat.Builder(context, SNOOZE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chrona_monochrome)
            .setContentTitle(title)
            .setContentText(contentText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        // PRIVATE version: shown when device is unlocked. Contains "Dismiss snoozed alarm" action!
        val privateNotification = NotificationCompat.Builder(context, SNOOZE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chrona_monochrome)
            .setContentTitle(title)
            .setContentText(contentText)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .addAction(
                R.drawable.ic_chrona_monochrome,
                "Dismiss snoozed alarm",
                cancelPendingIntent
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notifId, privateNotification)
    }

    fun cancelSnoozeNotification(context: Context, alarmId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val notifId = NOTIFICATION_ID_BASE + Math.abs(alarmId.hashCode() % 1000)
        notificationManager.cancel(notifId)
    }
}
