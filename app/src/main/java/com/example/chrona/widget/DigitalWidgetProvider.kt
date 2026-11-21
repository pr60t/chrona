package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class DigitalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateDigitalWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateDigitalWidgets(context)
    }
}
