package com.example.chrona

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.widget.WidgetUpdateHelper

class ChronaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val fastCache = getSharedPreferences("chrona_fast_cache", MODE_PRIVATE)
        val cachedThemeStr = fastCache.getString("cached_theme_mode", ThemeMode.SYSTEM.name)
        val cachedTheme = try {
            ThemeMode.valueOf(cachedThemeStr ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        PreferencesRepository.applyNightModeToSystem(this, cachedTheme)
        com.example.chrona.feature.stopwatch.StopwatchManager.init(this)
        com.example.chrona.feature.timer.TimerManager.init(this)

        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
                // When system dark/light theme changes and Chrona is set to Follow System, refresh all widgets
                val cache = getSharedPreferences("chrona_fast_cache", MODE_PRIVATE)
                val currentThemeStr = cache.getString("cached_theme_mode", ThemeMode.SYSTEM.name)
                val mode = try {
                    ThemeMode.valueOf(currentThemeStr ?: ThemeMode.SYSTEM.name)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                }
                if (mode == ThemeMode.SYSTEM) {
                    try {
                        WidgetUpdateHelper.updateAllWidgets(this@ChronaApplication)
                    } catch (_: Exception) {}
                }
            }

            @Deprecated("Deprecated in Java", ReplaceWith("onTrimMemory(TRIM_MEMORY_COMPLETE)"))
            override fun onLowMemory() {}
            override fun onTrimMemory(level: Int) {}
        })
    }
}
