package com.example.chrona.core.model

enum class ThemeMode {
    AMOLED_DARK,
    LIGHT,
    SYSTEM
}

enum class TimeFormatPreference {
    FOLLOW_SYSTEM,
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR
}

enum class ClockStyle {
    DIGITAL,
    ANALOG
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val timeFormatPreference: TimeFormatPreference = TimeFormatPreference.FOLLOW_SYSTEM,
    val is24HourFormat: Boolean = false,
    val accentColorHex: String = "#DCE2FA",
    val vibrationEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val defaultSnoozeDurationMinutes: Int = 10,
    val alarmVolumePercent: Int = 100,
    val alarmClockStyle: ClockStyle = ClockStyle.DIGITAL
)
