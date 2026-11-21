package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WorldClockAtlasWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateWorldClockAtlasWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateWorldClockAtlasWidgets(context)
    }
}
