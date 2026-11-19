package com.example.chrona.core.model

enum class ThemeMode {
    AMOLED_DARK,
    LIGHT,
    SYSTEM
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.AMOLED_DARK,
    val accentColorHex: String = "#93A8C7",
    val is24HourFormat: Boolean = false,
    val showCityImages: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)
