package com.example.chrona.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.chrona.core.data.AlarmRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            Log.d("BootReceiver", "Received broadcast: $action, rescheduling Chrona alarms")
            AlarmRepository.rescheduleAll(context)
            try {
                com.example.chrona.widget.WidgetUpdateHelper.updateAllWidgets(context)
            } catch (_: Exception) {}
        }
    }
}
