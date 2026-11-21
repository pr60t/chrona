package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WorldClockFrostWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateWorldClockFrostWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateWorldClockFrostWidgets(context)
    }
}
