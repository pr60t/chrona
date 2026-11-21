package com.example.chrona.core.model

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class City(
    val geonameId: Long,
    val cityName: String,
    val countryName: String,
    val countryCode: String,
    val admin1Name: String,
    val latitude: Double,
    val longitude: Double,
    val zoneIdString: String,
    val mapX: Float,
    val mapY: Float,
    val identityColorHex: String,
    val isSaved: Boolean = false,
    val population: Long = 0
) {
    val isUtc: Boolean
        get() = geonameId == 1L || zoneIdString == "Etc/UTC" || zoneIdString == "UTC" || cityName.equals("UTC", ignoreCase = true)

    val zoneId: ZoneId
        get() = try {
            if (isUtc) ZoneId.of("Etc/UTC") else ZoneId.of(zoneIdString)
        } catch (e: Exception) {
            ZoneId.of("Etc/UTC")
        }

    val identityColor: Color
        get() = try {
            val cleanHex = identityColorHex.removePrefix("#")
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } catch (e: Exception) {
            Color(0xFF93A8C7)
        }

    fun currentZonedDateTime(now: Instant = Instant.now()): ZonedDateTime {
        return now.atZone(zoneId)
    }

    fun isDaytime(now: Instant = Instant.now()): Boolean? {
        if (isUtc) return null
        return com.example.chrona.core.timezone.SolarCalculator.isDaytime(now, latitude, longitude)
    }

    fun localDate(now: Instant = Instant.now()): java.time.LocalDate {
        return currentZonedDateTime(now).toLocalDate()
    }

    fun formattedWeekday(now: Instant = Instant.now()): String {
        return currentZonedDateTime(now).dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
    }

    fun formattedFullDate(now: Instant = Instant.now()): String {
        return currentZonedDateTime(now).format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }

    fun formattedTime(is24Hour: Boolean = false, now: Instant = Instant.now()): String {
        val dt = currentZonedDateTime(now)
        val pattern = if (is24Hour) "HH:mm" else "h:mm"
        return dt.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun amPm(now: Instant = Instant.now()): String {
        val dt = currentZonedDateTime(now)
        return dt.format(DateTimeFormatter.ofPattern("a", Locale.getDefault())).uppercase()
    }

    fun formattedOffset(now: Instant = Instant.now()): String {
        val offset = zoneId.rules.getOffset(now)
        val totalSeconds = offset.totalSeconds
        if (totalSeconds == 0) return "UTC"
        val hours = totalSeconds / 3600
        val minutes = kotlin.math.abs((totalSeconds % 3600) / 60)
        val sign = if (hours >= 0) "+" else "-"
        val absHours = kotlin.math.abs(hours)
        return if (minutes == 0) "UTC$sign$absHours" else "UTC$sign$absHours:${minutes.toString().padStart(2, '0')}"
    }

    fun relativeTimeDifference(deviceZoneId: ZoneId = ZoneId.systemDefault(), now: Instant = Instant.now()): String {
        val cityOffset = zoneId.rules.getOffset(now).totalSeconds
        val deviceOffset = deviceZoneId.rules.getOffset(now).totalSeconds
        val diffSeconds = cityOffset - deviceOffset
        if (diffSeconds == 0) return "Same time"
        val diffHours = diffSeconds / 3600.0
        val isAhead = diffHours > 0
        val absHours = kotlin.math.abs(diffHours)
        val wholeHours = absHours.toInt()
        val minutes = ((absHours - wholeHours) * 60).toInt()

        val timeStr = if (minutes == 0) "${wholeHours}h" else "${wholeHours}h ${minutes}m"
        return if (isAhead) "$timeStr ahead" else "$timeStr behind"
    }
}
