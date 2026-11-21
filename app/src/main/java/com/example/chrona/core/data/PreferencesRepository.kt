package com.example.chrona.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chrona.core.model.ClockStyle
import com.example.chrona.core.model.ThemeMode
import com.example.chrona.core.model.TimeFormatPreference
import com.example.chrona.core.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chrona_prefs")

object PreferencesRepository {
    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_ACCENT = stringPreferencesKey("accent_color")
    private val KEY_24H = booleanPreferencesKey("is_24_hour")
    private val KEY_TIME_FORMAT_PREF = stringPreferencesKey("time_format_preference")
    private val KEY_VIBRATE = booleanPreferencesKey("vibration_enabled")
    private val KEY_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    private val KEY_DEFAULT_SNOOZE = intPreferencesKey("default_snooze_duration")
    private val KEY_ALARM_VOLUME = intPreferencesKey("alarm_volume_percent")
    private val KEY_CLOCK_STYLE = stringPreferencesKey("alarm_clock_style")

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun resolveIs24Hour(context: Context, pref: TimeFormatPreference): Boolean {
        return when (pref) {
            TimeFormatPreference.FOLLOW_SYSTEM -> android.text.format.DateFormat.is24HourFormat(context)
            TimeFormatPreference.TWELVE_HOUR -> false
            TimeFormatPreference.TWENTY_FOUR_HOUR -> true
        }
    }

    const val DEFAULT_ACCENT = "#DCE2FA"
    private const val LEGACY_DEFAULT_ACCENT = "#93A8C7"

    fun init(context: Context) {
        // Fast synchronous cache read to populate theme immediately on cold start without delay or flash
        val fastCache = context.getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
        val cachedThemeStr = fastCache.getString("cached_theme_mode", null)
        val initialTheme = if (cachedThemeStr != null) {
            try { ThemeMode.valueOf(cachedThemeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        } else {
            ThemeMode.SYSTEM
        }
        val isUserExplicit = fastCache.getBoolean("user_explicitly_set_accent", false)
        val rawCachedAccent = fastCache.getString("cached_accent_color", null)
        val cachedAccent = when {
            rawCachedAccent == null -> DEFAULT_ACCENT
            rawCachedAccent.equals(LEGACY_DEFAULT_ACCENT, ignoreCase = true) && !isUserExplicit -> DEFAULT_ACCENT
            else -> rawCachedAccent
        }
        val cachedTimeFormatPrefStr = fastCache.getString("cached_time_format_pref", TimeFormatPreference.FOLLOW_SYSTEM.name)
        val initialTimeFormatPref = try {
            TimeFormatPreference.valueOf(cachedTimeFormatPrefStr ?: TimeFormatPreference.FOLLOW_SYSTEM.name)
        } catch (_: Exception) {
            TimeFormatPreference.FOLLOW_SYSTEM
        }
        val initialIs24H = resolveIs24Hour(context, initialTimeFormatPref)

        _preferences.value = _preferences.value.copy(
            themeMode = initialTheme,
            accentColorHex = cachedAccent,
            timeFormatPreference = initialTimeFormatPref,
            is24HourFormat = initialIs24H
        )

        applyNightModeToSystem(context, initialTheme)

        scope.launch {
            try {
                val prefs = context.dataStore.data.first()
                val themeStr = prefs[KEY_THEME] ?: cachedThemeStr ?: ThemeMode.SYSTEM.name
                val theme = try { ThemeMode.valueOf(themeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
                val rawDataStoreAccent = prefs[KEY_ACCENT]
                val accent = when {
                    rawDataStoreAccent == null -> cachedAccent
                    rawDataStoreAccent.equals(LEGACY_DEFAULT_ACCENT, ignoreCase = true) && !isUserExplicit -> DEFAULT_ACCENT
                    else -> rawDataStoreAccent
                }
                val timeFormatPrefStr = prefs[KEY_TIME_FORMAT_PREF] ?: cachedTimeFormatPrefStr ?: TimeFormatPreference.FOLLOW_SYSTEM.name
                val timeFormatPref = try { TimeFormatPreference.valueOf(timeFormatPrefStr) } catch (_: Exception) { TimeFormatPreference.FOLLOW_SYSTEM }
                val resolved24H = resolveIs24Hour(context, timeFormatPref)
                val vibrate = prefs[KEY_VIBRATE] ?: true
                val onboarding = prefs[KEY_ONBOARDING] ?: false
                val defaultSnooze = prefs[KEY_DEFAULT_SNOOZE] ?: 10
                val volume = prefs[KEY_ALARM_VOLUME] ?: 100
                val styleStr = prefs[KEY_CLOCK_STYLE] ?: ClockStyle.DIGITAL.name
                val style = try { ClockStyle.valueOf(styleStr) } catch (_: Exception) { ClockStyle.DIGITAL }

                // Keep fast cache in sync
                fastCache.edit()
                    .putString("cached_theme_mode", theme.name)
                    .putString("cached_accent_color", accent)
                    .putString("cached_time_format_pref", timeFormatPref.name)
                    .apply()

                applyNightModeToSystem(context, theme)

                _preferences.value = UserPreferences(
                    themeMode = theme,
                    timeFormatPreference = timeFormatPref,
                    accentColorHex = accent,
                    is24HourFormat = resolved24H,
                    vibrationEnabled = vibrate,
                    hasCompletedOnboarding = onboarding,
                    defaultSnoozeDurationMinutes = defaultSnooze.coerceIn(1, 30),
                    alarmVolumePercent = volume.coerceIn(0, 100),
                    alarmClockStyle = style
                )
            } catch (_: Exception) {
                // Fallback to defaults
            } finally {
                _isInitialized.value = true
            }
        }
    }

    fun applyNightModeToSystem(context: Context, mode: ThemeMode) {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        uiModeManager?.let {
            val targetMode = when (mode) {
                ThemeMode.AMOLED_DARK -> android.app.UiModeManager.MODE_NIGHT_YES
                ThemeMode.LIGHT -> android.app.UiModeManager.MODE_NIGHT_NO
                ThemeMode.SYSTEM -> android.app.UiModeManager.MODE_NIGHT_AUTO
            }
            try {
                it.setApplicationNightMode(targetMode)
            } catch (e: Exception) {
                // Ignore if not supported
            }
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        _preferences.value = _preferences.value.copy(themeMode = mode)
        context.getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
            .edit()
            .putString("cached_theme_mode", mode.name)
            .apply()
        applyNightModeToSystem(context, mode)
        scope.launch {
            context.dataStore.edit { it[KEY_THEME] = mode.name }
        }
        try {
            com.example.chrona.widget.WidgetUpdateHelper.updateAllWidgets(context)
        } catch (_: Exception) {}
    }

    fun setAccentColor(context: Context, hex: String) {
        _preferences.value = _preferences.value.copy(accentColorHex = hex)
        context.getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
            .edit()
            .putString("cached_accent_color", hex)
            .putBoolean("user_explicitly_set_accent", true)
            .apply()
        scope.launch {
            context.dataStore.edit { it[KEY_ACCENT] = hex }
        }
        try {
            com.example.chrona.widget.WidgetUpdateHelper.updateAllWidgets(context)
        } catch (_: Exception) {}
    }

    fun setTimeFormatPreference(context: Context, pref: TimeFormatPreference) {
        val resolved = resolveIs24Hour(context, pref)
        _preferences.value = _preferences.value.copy(
            timeFormatPreference = pref,
            is24HourFormat = resolved
        )
        context.getSharedPreferences("chrona_fast_cache", Context.MODE_PRIVATE)
            .edit()
            .putString("cached_time_format_pref", pref.name)
            .apply()
        scope.launch {
            context.dataStore.edit {
                it[KEY_TIME_FORMAT_PREF] = pref.name
                it[KEY_24H] = resolved
            }
        }
    }

    fun set24HourFormat(context: Context, is24Hour: Boolean) {
        val pref = if (is24Hour) TimeFormatPreference.TWENTY_FOUR_HOUR else TimeFormatPreference.TWELVE_HOUR
        setTimeFormatPreference(context, pref)
    }

    @Deprecated("City images removed in Phase 4")
    fun setShowCityImages(context: Context, show: Boolean) {
        // Safe no-op for backwards compatibility
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        _preferences.value = _preferences.value.copy(vibrationEnabled = enabled)
        scope.launch {
            context.dataStore.edit { it[KEY_VIBRATE] = enabled }
        }
    }

    fun setDefaultSnoozeDuration(context: Context, minutes: Int) {
        val clamped = minutes.coerceIn(1, 30)
        _preferences.value = _preferences.value.copy(defaultSnoozeDurationMinutes = clamped)
        scope.launch {
            context.dataStore.edit { it[KEY_DEFAULT_SNOOZE] = clamped }
        }
    }

    fun setAlarmVolumePercent(context: Context, percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _preferences.value = _preferences.value.copy(alarmVolumePercent = clamped)
        scope.launch {
            context.dataStore.edit { it[KEY_ALARM_VOLUME] = clamped }
        }
    }

    fun setAlarmClockStyle(context: Context, style: ClockStyle) {
        _preferences.value = _preferences.value.copy(alarmClockStyle = style)
        scope.launch {
            context.dataStore.edit { it[KEY_CLOCK_STYLE] = style.name }
        }
    }

    fun completeOnboarding(context: Context) {
        _preferences.value = _preferences.value.copy(hasCompletedOnboarding = true)
        scope.launch {
            context.dataStore.edit { it[KEY_ONBOARDING] = true }
        }
    }
}
