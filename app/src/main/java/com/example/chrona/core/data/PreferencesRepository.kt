package com.example.chrona.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chrona.core.model.ThemeMode
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
    private val KEY_SHOW_IMAGES = booleanPreferencesKey("show_city_images")
    private val KEY_VIBRATE = booleanPreferencesKey("vibration_enabled")
    private val KEY_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        scope.launch {
            try {
                val prefs = context.dataStore.data.first()
                val themeStr = prefs[KEY_THEME] ?: ThemeMode.AMOLED_DARK.name
                val theme = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.AMOLED_DARK }
                val accent = prefs[KEY_ACCENT] ?: "#93A8C7"
                val is24H = prefs[KEY_24H] ?: false
                val showImages = prefs[KEY_SHOW_IMAGES] ?: true
                val vibrate = prefs[KEY_VIBRATE] ?: true
                val onboarding = prefs[KEY_ONBOARDING] ?: false

                _preferences.value = UserPreferences(
                    themeMode = theme,
                    accentColorHex = accent,
                    is24HourFormat = is24H,
                    showCityImages = showImages,
                    vibrationEnabled = vibrate,
                    hasCompletedOnboarding = onboarding
                )
            } catch (e: Exception) {
                // Fallback to default
            }
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        _preferences.value = _preferences.value.copy(themeMode = mode)
        scope.launch {
            context.dataStore.edit { it[KEY_THEME] = mode.name }
        }
    }

    fun setAccentColor(context: Context, hex: String) {
        _preferences.value = _preferences.value.copy(accentColorHex = hex)
        scope.launch {
            context.dataStore.edit { it[KEY_ACCENT] = hex }
        }
    }

    fun set24HourFormat(context: Context, is24Hour: Boolean) {
        _preferences.value = _preferences.value.copy(is24HourFormat = is24Hour)
        scope.launch {
            context.dataStore.edit { it[KEY_24H] = is24Hour }
        }
    }

    fun setShowCityImages(context: Context, show: Boolean) {
        _preferences.value = _preferences.value.copy(showCityImages = show)
        scope.launch {
            context.dataStore.edit { it[KEY_SHOW_IMAGES] = show }
        }
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        _preferences.value = _preferences.value.copy(vibrationEnabled = enabled)
        scope.launch {
            context.dataStore.edit { it[KEY_VIBRATE] = enabled }
        }
    }

    fun completeOnboarding(context: Context) {
        _preferences.value = _preferences.value.copy(hasCompletedOnboarding = true)
        scope.launch {
            context.dataStore.edit { it[KEY_ONBOARDING] = true }
        }
    }
}
