package com.example.chrona.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.chrona.MainActivity
import com.example.chrona.R
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.database.AlarmDao
import com.example.chrona.core.database.CityCatalogueDatabase
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.City
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.core.timezone.TimezoneEngine
import com.example.chrona.feature.stopwatch.StopwatchManager
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

object WidgetUpdateHelper {

    private const val PREFS_NAME = "chrona_widget_prefs"
    private const val KEY_PREFIX_CITIES = "widget_cities_"

    /**
     * Resolves whether widgets should display their dark or light design.
     * Priority:
     * 1. AMOLED_DARK -> always dark
     * 2. LIGHT -> always light
     * 3. SYSTEM -> follow current Android system UI night mode
     */
    fun isWidgetDark(context: Context): Boolean {
        val prefs = context.getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
        val cachedThemeStr = prefs.getString("cached_theme_mode", null)
        val themeMode = if (cachedThemeStr != null) {
            try {
                ThemeMode.valueOf(cachedThemeStr)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        } else {
            PreferencesRepository.preferences.value.themeMode
        }

        return when (themeMode) {
            ThemeMode.AMOLED_DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> {
                val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                uiMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    fun getWidgetCities(context: Context, appWidgetId: Int): List<City> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawIds = prefs.getString("$KEY_PREFIX_CITIES$appWidgetId", null)
        val cityCatalogue = CityCatalogueDatabase.getInstance(context)

        val cityIds = if (!rawIds.isNullOrEmpty()) {
            rawIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        } else {
            // Default 4 cities in exact order: UTC, Tokyo, Paris, Munich
            CityRepository.defaultCityIds
        }

        val allSaved = CityRepository.savedCities.value
        val result = mutableListOf<City>()
        for (id in cityIds) {
            val fromSaved = allSaved.find { it.geonameId == id }
            val city = fromSaved ?: cityCatalogue.getCityById(id)
            if (city != null && !result.any { it.geonameId == city.geonameId }) {
                result.add(city)
            }
        }

        if (result.isEmpty()) {
            CityRepository.defaultCityIds.forEach { id ->
                cityCatalogue.getCityById(id)?.let { result.add(it) }
            }
        }

        return result
    }

    fun saveWidgetCities(context: Context, appWidgetId: Int, cityIds: List<Long>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = cityIds.take(4).joinToString(",")
        prefs.edit().putString("$KEY_PREFIX_CITIES$appWidgetId", serialized).apply()
    }

    fun getNextUpcomingAlarm(context: Context): Pair<Alarm, Instant>? {
        val alarms = try {
            val fromRepo = AlarmRepository.alarms.value
            if (fromRepo.isNotEmpty()) fromRepo else AlarmDao(context).getAll()
        } catch (_: Exception) {
            emptyList()
        }

        return alarms
            .filter { it.isEnabled }
            .mapNotNull { alarm ->
                TimezoneEngine.calculateNextTriggerInstant(alarm)?.let { instant ->
                    alarm to instant
                }
            }
            .minByOrNull { it.second }
    }

    fun formatOffsetHours(zoneId: ZoneId): String {
        return try {
            val now = Instant.now()
            val offsetSeconds = zoneId.rules.getOffset(now).totalSeconds
            val hours = offsetSeconds / 3600
            val sign = if (hours >= 0) "+" else ""
            "${sign}${hours}H"
        } catch (_: Exception) {
            "+0H"
        }
    }

    fun getSolarIndicatorColor(city: City, isDark: Boolean = true): Int {
        if (city.isUtc) {
            return if (isDark) 0xFF7C8292.toInt() else 0xFF888B98.toInt()
        }
        val isDay = city.isDaytime()
        return if (isDay == true) {
            if (isDark) 0xFFE8B872.toInt() else 0xFFD99B3C.toInt()
        } else {
            if (isDark) 0xFF96A8E8.toInt() else 0xFF6B7FD4.toInt()
        }
    }

    fun formatStopwatchDisplay(elapsedMillis: Long): String {
        val totalSeconds = elapsedMillis / 1000
        val minutes = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun updateAllWidgets(context: Context) {
        updateAnalogueWidgets(context)
        updateDigitalWidgets(context)
        updateStackedWidgets(context)
        updateStopwatchWidgets(context)
        updateWorldClockAtlasWidgets(context)
        updateWorldClockMinimalWidgets(context)
        updateWorldClockFrostWidgets(context)
    }

    fun updateAnalogueWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, AnalogueWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_analogue else R.layout.widget_analogue_light

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "WORLD_CLOCK")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_analogue_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateDigitalWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, DigitalWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_digital else R.layout.widget_digital_light
        val is24H = PreferencesRepository.resolveIs24Hour(
            context,
            PreferencesRepository.preferences.value.timeFormatPreference
        )
        val nextAlarm = getNextUpcomingAlarm(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)

            // Formats
            views.setCharSequence(R.id.digital_date, "setFormat12Hour", "EEE, MMM d")
            views.setCharSequence(R.id.digital_date, "setFormat24Hour", "EEE, MMM d")
            views.setCharSequence(R.id.digital_time, "setFormat12Hour", if (is24H) "HH:mm" else "h:mm")
            views.setCharSequence(R.id.digital_time, "setFormat24Hour", "HH:mm")

            // Next Alarm row
            if (nextAlarm != null) {
                views.setViewVisibility(R.id.digital_alarm_row, View.VISIBLE)
                val alarmTimeStr = nextAlarm.first.formattedTargetTime(is24H)
                views.setTextViewText(R.id.digital_alarm_text, alarmTimeStr)
            } else {
                views.setViewVisibility(R.id.digital_alarm_row, View.GONE)
            }

            // Click opens Alarms
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "ALARMS")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_digital_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateStackedWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, StackedWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_stacked else R.layout.widget_stacked_light
        val is24H = PreferencesRepository.resolveIs24Hour(
            context,
            PreferencesRepository.preferences.value.timeFormatPreference
        )
        val nextAlarm = getNextUpcomingAlarm(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)

            views.setCharSequence(R.id.stacked_date, "setFormat12Hour", "EEE, MMM d")
            views.setCharSequence(R.id.stacked_date, "setFormat24Hour", "EEE, MMM d")
            views.setCharSequence(R.id.stacked_hours, "setFormat12Hour", if (is24H) "HH" else "hh")
            views.setCharSequence(R.id.stacked_hours, "setFormat24Hour", "HH")
            views.setCharSequence(R.id.stacked_minutes, "setFormat12Hour", "mm")
            views.setCharSequence(R.id.stacked_minutes, "setFormat24Hour", "mm")

            if (nextAlarm != null) {
                views.setViewVisibility(R.id.stacked_alarm_row, View.VISIBLE)
                val alarmTimeStr = nextAlarm.first.formattedTargetTime(is24H)
                views.setTextViewText(R.id.stacked_alarm_text, alarmTimeStr)
            } else {
                views.setViewVisibility(R.id.stacked_alarm_row, View.GONE)
            }

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "ALARMS")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_stacked_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateStopwatchWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, StopwatchWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_stopwatch else R.layout.widget_stopwatch_light
        val isRunning = StopwatchManager.isRunning.value
        val elapsed = StopwatchManager.getElapsedMillis()

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)

            if (isRunning) {
                views.setViewVisibility(R.id.stopwatch_chronometer, View.VISIBLE)
                views.setViewVisibility(R.id.stopwatch_time_text, View.GONE)
                val base = SystemClock.elapsedRealtime() - elapsed
                views.setChronometer(R.id.stopwatch_chronometer, base, null, true)
                views.setImageViewResource(
                    R.id.btn_play_pause_icon,
                    if (isDark) R.drawable.ic_widget_pause else R.drawable.ic_widget_pause_light
                )
            } else {
                views.setViewVisibility(R.id.stopwatch_chronometer, View.GONE)
                views.setViewVisibility(R.id.stopwatch_time_text, View.VISIBLE)
                views.setTextViewText(R.id.stopwatch_time_text, formatStopwatchDisplay(elapsed))
                views.setImageViewResource(
                    R.id.btn_play_pause_icon,
                    if (isDark) R.drawable.ic_widget_play else R.drawable.ic_widget_play_light
                )
            }

            // Play/Pause Action
            val playIntent = Intent(context, StopwatchWidgetProvider::class.java).apply {
                action = StopwatchWidgetProvider.ACTION_STOPWATCH_TOGGLE
            }
            val playPendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_play_pause, playPendingIntent)

            // Reset Action
            val resetIntent = Intent(context, StopwatchWidgetProvider::class.java).apply {
                action = StopwatchWidgetProvider.ACTION_STOPWATCH_RESET
            }
            val resetPendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_reset, resetPendingIntent)

            // Lap Action
            val lapIntent = Intent(context, StopwatchWidgetProvider::class.java).apply {
                action = StopwatchWidgetProvider.ACTION_STOPWATCH_LAP
            }
            val lapPendingIntent = PendingIntent.getBroadcast(
                context,
                1003,
                lapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_lap, lapPendingIntent)

            // Dial Click -> Open Stopwatch Screen
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "STOPWATCH")
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.stopwatch_dial, openPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateWorldClockAtlasWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, WorldClockAtlasWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_world_clock_atlas else R.layout.widget_world_clock_atlas_light

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)
            val cities = getWidgetCities(context, appWidgetId)

            bindAtlasCell(views, 1, cities.getOrNull(0), isDark)
            bindAtlasCell(views, 2, cities.getOrNull(1), isDark)
            bindAtlasCell(views, 3, cities.getOrNull(2), isDark)
            bindAtlasCell(views, 4, cities.getOrNull(3), isDark)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "WORLD_CLOCK")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_atlas_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun bindAtlasCell(views: RemoteViews, cellIndex: Int, city: City?, isDark: Boolean) {
        val (rootId, pixelId, cityId, timeId, dayId) = when (cellIndex) {
            1 -> listOf(R.id.atlas_cell1_root, R.id.atlas_cell1_pixel, R.id.atlas_cell1_city, R.id.atlas_cell1_time, R.id.atlas_cell1_day)
            2 -> listOf(R.id.atlas_cell2_root, R.id.atlas_cell2_pixel, R.id.atlas_cell2_city, R.id.atlas_cell2_time, R.id.atlas_cell2_day)
            3 -> listOf(R.id.atlas_cell3_root, R.id.atlas_cell3_pixel, R.id.atlas_cell3_city, R.id.atlas_cell3_time, R.id.atlas_cell3_day)
            else -> listOf(R.id.atlas_cell4_root, R.id.atlas_cell4_pixel, R.id.atlas_cell4_city, R.id.atlas_cell4_time, R.id.atlas_cell4_day)
        }

        if (city == null) {
            views.setViewVisibility(rootId, View.INVISIBLE)
            return
        }

        views.setViewVisibility(rootId, View.VISIBLE)
        views.setTextViewText(cityId, city.cityName.uppercase(Locale.US))
        views.setString(timeId, "setTimeZone", city.zoneIdString)
        views.setCharSequence(timeId, "setFormat12Hour", "h:mm")
        views.setCharSequence(timeId, "setFormat24Hour", "HH:mm")

        val zonedDateTime = ZonedDateTime.now(city.zoneId)
        val dayName = zonedDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(Locale.US)
        val offsetStr = formatOffsetHours(city.zoneId)
        views.setTextViewText(dayId, "$dayName · $offsetStr")

        val pixelColor = getSolarIndicatorColor(city, isDark)
        views.setInt(pixelId, "setColorFilter", pixelColor)
    }

    fun updateWorldClockMinimalWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, WorldClockMinimalWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_world_clock_minimal else R.layout.widget_world_clock_minimal_light

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)
            val cities = getWidgetCities(context, appWidgetId)

            bindMinimalCell(views, 1, cities.getOrNull(0), isDark)
            bindMinimalCell(views, 2, cities.getOrNull(1), isDark)
            bindMinimalCell(views, 3, cities.getOrNull(2), isDark)
            bindMinimalCell(views, 4, cities.getOrNull(3), isDark)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "WORLD_CLOCK")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_minimal_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun bindMinimalCell(views: RemoteViews, cellIndex: Int, city: City?, isDark: Boolean) {
        val (rootId, pixelId, cityId, timeId) = when (cellIndex) {
            1 -> listOf(R.id.minimal_cell1_root, R.id.minimal_cell1_pixel, R.id.minimal_cell1_city, R.id.minimal_cell1_time)
            2 -> listOf(R.id.minimal_cell2_root, R.id.minimal_cell2_pixel, R.id.minimal_cell2_city, R.id.minimal_cell2_time)
            3 -> listOf(R.id.minimal_cell3_root, R.id.minimal_cell3_pixel, R.id.minimal_cell3_city, R.id.minimal_cell3_time)
            else -> listOf(R.id.minimal_cell4_root, R.id.minimal_cell4_pixel, R.id.minimal_cell4_city, R.id.minimal_cell4_time)
        }

        if (city == null) {
            views.setViewVisibility(rootId, View.INVISIBLE)
            return
        }

        views.setViewVisibility(rootId, View.VISIBLE)
        views.setTextViewText(cityId, city.cityName.uppercase(Locale.US))
        views.setString(timeId, "setTimeZone", city.zoneIdString)
        views.setCharSequence(timeId, "setFormat12Hour", "h:mm")
        views.setCharSequence(timeId, "setFormat24Hour", "HH:mm")

        val pixelColor = getSolarIndicatorColor(city, isDark)
        views.setInt(pixelId, "setColorFilter", pixelColor)
    }

    fun updateWorldClockFrostWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, WorldClockFrostWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
        if (appWidgetIds.isEmpty()) return

        val isDark = isWidgetDark(context)
        val layoutRes = if (isDark) R.layout.widget_world_clock_frost else R.layout.widget_world_clock_frost_light

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutRes)
            val cities = getWidgetCities(context, appWidgetId)

            bindFrostCell(views, 1, cities.getOrNull(0), isDark)
            bindFrostCell(views, 2, cities.getOrNull(1), isDark)
            bindFrostCell(views, 3, cities.getOrNull(2), isDark)
            bindFrostCell(views, 4, cities.getOrNull(3), isDark)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_DESTINATION", "WORLD_CLOCK")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_frost_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun bindFrostCell(views: RemoteViews, cellIndex: Int, city: City?, isDark: Boolean) {
        val (rootId, pixelId, cityId, timeId, dayId) = when (cellIndex) {
            1 -> listOf(R.id.frost_cell1_root, R.id.frost_cell1_pixel, R.id.frost_cell1_city, R.id.frost_cell1_time, R.id.frost_cell1_day)
            2 -> listOf(R.id.frost_cell2_root, R.id.frost_cell2_pixel, R.id.frost_cell2_city, R.id.frost_cell2_time, R.id.frost_cell2_day)
            3 -> listOf(R.id.frost_cell3_root, R.id.frost_cell3_pixel, R.id.frost_cell3_city, R.id.frost_cell3_time, R.id.frost_cell3_day)
            else -> listOf(R.id.frost_cell4_root, R.id.frost_cell4_pixel, R.id.frost_cell4_city, R.id.frost_cell4_time, R.id.frost_cell4_day)
        }

        if (city == null) {
            views.setViewVisibility(rootId, View.INVISIBLE)
            return
        }

        views.setViewVisibility(rootId, View.VISIBLE)
        views.setTextViewText(cityId, city.cityName.uppercase(Locale.US))
        views.setString(timeId, "setTimeZone", city.zoneIdString)
        views.setCharSequence(timeId, "setFormat12Hour", "h:mm")
        views.setCharSequence(timeId, "setFormat24Hour", "HH:mm")

        val zonedDateTime = ZonedDateTime.now(city.zoneId)
        val dayName = zonedDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(Locale.US)
        val offsetStr = formatOffsetHours(city.zoneId)
        views.setTextViewText(dayId, "$dayName · $offsetStr")

        val pixelColor = getSolarIndicatorColor(city, isDark)
        views.setInt(pixelId, "setColorFilter", pixelColor)
    }
}
