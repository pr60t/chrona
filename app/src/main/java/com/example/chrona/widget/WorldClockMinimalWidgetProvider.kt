package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WorldClockMinimalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateWorldClockMinimalWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateWorldClockMinimalWidgets(context)
    }
}
