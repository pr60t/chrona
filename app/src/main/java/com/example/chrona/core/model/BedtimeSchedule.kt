package com.example.chrona.core.model

data class BedtimeSchedule(
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val wakeHour: Int = 6,
    val wakeMinute: Int = 30,
    val isEnabled: Boolean = true,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5), // Mon-Fri
    val reminderMinutesBefore: Int = 15,
    val vibrate: Boolean = true
) {
    val sleepDurationHours: Float
        get() {
            var diffMinutes = (wakeHour * 60 + wakeMinute) - (bedtimeHour * 60 + bedtimeMinute)
            if (diffMinutes <= 0) {
                diffMinutes += 24 * 60
            }
            return diffMinutes / 60f
        }

    fun formattedSleepDuration(): String {
        var diffMinutes = (wakeHour * 60 + wakeMinute) - (bedtimeHour * 60 + bedtimeMinute)
        if (diffMinutes <= 0) {
            diffMinutes += 24 * 60
        }
        val h = diffMinutes / 60
        val m = diffMinutes % 60
        return if (m == 0) "${h}h" else "${h}h ${m}m"
    }

    fun formattedBedtime(is24Hour: Boolean = false): String {
        return formatTime(bedtimeHour, bedtimeMinute, is24Hour)
    }

    fun formattedWakeTime(is24Hour: Boolean = false): String {
        return formatTime(wakeHour, wakeMinute, is24Hour)
    }

    private fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour >= 12) "PM" else "AM"
            String.format("%d:%02d %s", displayHour, minute, amPm)
        }
    }
}
