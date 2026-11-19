package com.example.chrona.core.model

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

enum class DstPolicy {
    NEXT_VALID_TIME,
    SKIP_OCCURRENCE,
    FIRST_OCCURRENCE, // For fall-back overlap
    SECOND_OCCURRENCE
}

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Alarm",
    val hour: Int, // 0..23 in target timezone
    val minute: Int, // 0..59 in target timezone
    val targetZoneIdString: String = "SYSTEM", // "SYSTEM" means device local, or IANA string like "Asia/Tokyo"
    val targetCityName: String = "Local Time",
    val targetCountryName: String = "",
    val identityColorHex: String = "#93A8C7",
    val daysOfWeek: Set<Int> = emptySet(), // 1=Monday ... 7=Sunday. Empty = one-off alarm.
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val snoozeDurationMinutes: Int = 10,
    val gradualVolumeRamp: Boolean = true,
    val dstPolicy: DstPolicy = DstPolicy.NEXT_VALID_TIME,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    val isCrossTimezone: Boolean
        get() = targetZoneIdString != "SYSTEM" && targetZoneIdString != ZoneId.systemDefault().id

    val targetZoneId: ZoneId
        get() = if (targetZoneIdString == "SYSTEM") ZoneId.systemDefault() else {
            try {
                ZoneId.of(targetZoneIdString)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }
        }

    val localTime: LocalTime
        get() = LocalTime.of(hour, minute)

    val identityColor: Color
        get() = try {
            val cleanHex = identityColorHex.removePrefix("#")
            val parsed = cleanHex.toLongOrNull(16) ?: 0xFF93A8C7L
            val argb = if (cleanHex.length <= 6) 0xFF000000L or parsed else parsed
            Color(argb.toInt())
        } catch (e: Exception) {
            Color(0xFF93A8C7)
        }

    fun formattedTargetTime(is24Hour: Boolean = false): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format("%d:%02d", displayHour, minute)
        }
    }

    fun targetAmPm(): String {
        return if (hour >= 12) "PM" else "AM"
    }

    fun repeatSummary(): String {
        if (daysOfWeek.isEmpty()) return "Once"
        if (daysOfWeek.size == 7) return "Every day"
        if (daysOfWeek == setOf(1, 2, 3, 4, 5)) return "Weekdays"
        if (daysOfWeek == setOf(6, 7)) return "Weekends"
        val dayNames = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return daysOfWeek.sorted().joinToString(" ") { dayNames.getOrElse(it) { "" } }
    }
}
