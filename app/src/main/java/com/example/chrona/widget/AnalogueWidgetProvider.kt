package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class AnalogueWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateAnalogueWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateAnalogueWidgets(context)
    }
}
