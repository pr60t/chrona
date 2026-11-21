package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.example.chrona.feature.stopwatch.StopwatchManager

class StopwatchWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_STOPWATCH_TOGGLE = "com.example.chrona.widget.ACTION_STOPWATCH_TOGGLE"
        const val ACTION_STOPWATCH_RESET = "com.example.chrona.widget.ACTION_STOPWATCH_RESET"
        const val ACTION_STOPWATCH_LAP = "com.example.chrona.widget.ACTION_STOPWATCH_LAP"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        StopwatchManager.init(context)

        when (intent.action) {
            ACTION_STOPWATCH_TOGGLE -> {
                if (StopwatchManager.isRunning.value) {
                    StopwatchManager.pause()
                } else {
                    StopwatchManager.start()
                }
                WidgetUpdateHelper.updateStopwatchWidgets(context)
            }
            ACTION_STOPWATCH_RESET -> {
                StopwatchManager.reset()
                WidgetUpdateHelper.updateStopwatchWidgets(context)
            }
            ACTION_STOPWATCH_LAP -> {
                if (StopwatchManager.isRunning.value) {
                    StopwatchManager.recordLap()
                    WidgetUpdateHelper.updateStopwatchWidgets(context)
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        StopwatchManager.init(context)
        WidgetUpdateHelper.updateStopwatchWidgets(context)
    }

    override fun onEnabled(context: Context) {
        StopwatchManager.init(context)
        WidgetUpdateHelper.updateStopwatchWidgets(context)
    }
}
