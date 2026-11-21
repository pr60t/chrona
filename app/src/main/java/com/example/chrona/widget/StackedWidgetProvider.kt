package com.example.chrona.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class StackedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateStackedWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateStackedWidgets(context)
    }
}
