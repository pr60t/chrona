package com.example.chrona.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.chrona.MainActivity
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.timezone.TimezoneEngine

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val ACTION_ALARM_TRIGGER = "com.example.chrona.ACTION_ALARM_TRIGGER"
    const val EXTRA_ALARM_ID = "extra_alarm_id"

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm)
            return
        }

        val triggerInstant = TimezoneEngine.calculateNextTriggerInstant(alarm) ?: return
        val triggerEpochMillis = triggerInstant.toEpochMilli()
        scheduleExactAlarm(context, alarm.id, alarm.label, triggerEpochMillis)
    }

    fun scheduleSnooze(context: Context, snoozeAlarm: Alarm, snoozeMinutes: Int) {
        val triggerEpochMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        scheduleExactAlarm(context, snoozeAlarm.id, snoozeAlarm.label, triggerEpochMillis)
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmId: String,
        label: String,
        triggerEpochMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("RINGING_ALARM_ID", alarmId)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Scheduling alarm '$label' ($alarmId) for epoch $triggerEpochMillis")

        try {
            val clockInfo = AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm clock: permission missing", e)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule inexact alarm", e2)
            }
        }
    }

    fun cancelAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm '${alarm.label}' (${alarm.id})")
        }
    }
}
