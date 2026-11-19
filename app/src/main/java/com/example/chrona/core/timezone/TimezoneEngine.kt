package com.example.chrona.core.timezone

import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.DstPolicy
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRules
import java.util.Locale

data class DstStatus(
    val isGap: Boolean,
    val isOverlap: Boolean,
    val message: String? = null,
    val suggestedTime: LocalTime? = null
)

data class LocalPreview(
    val deviceTime: String,
    val deviceAmPm: String,
    val dayOffsetDescription: String, // e.g. "Today", "Tomorrow", "Next Monday"
    val instant: Instant
)

object TimezoneEngine {

    /**
     * Checks if a given target local time and date falls within a DST gap or overlap.
     */
    fun checkDstStatus(
        targetZoneId: ZoneId,
        date: LocalDate,
        time: LocalTime
    ): DstStatus {
        val rules: ZoneRules = targetZoneId.rules
        val ldt = LocalDateTime.of(date, time)
        val validOffsets = rules.getValidOffsets(ldt)

        return when {
            validOffsets.isEmpty() -> {
                // Gap (Spring Forward): local time does not exist
                val transition = rules.getTransition(ldt)
                val advanceSeconds = transition?.duration?.seconds ?: 3600
                val nextValid = ldt.plusSeconds(advanceSeconds).toLocalTime()
                DstStatus(
                    isGap = true,
                    isOverlap = false,
                    message = "This local time does not exist in ${targetZoneId.id} due to Daylight Saving Time spring-forward.",
                    suggestedTime = nextValid
                )
            }
            validOffsets.size > 1 -> {
                // Overlap (Fall Back): local time occurs twice
                DstStatus(
                    isGap = false,
                    isOverlap = true,
                    message = "This local time occurs twice in ${targetZoneId.id} due to Daylight Saving Time fall-back."
                )
            }
            else -> DstStatus(isGap = false, isOverlap = false)
        }
    }

    /**
     * Resolves a target LocalDateTime into a valid Instant according to DST policies.
     */
    fun resolveInstant(
        targetZoneId: ZoneId,
        date: LocalDate,
        time: LocalTime,
        policy: DstPolicy
    ): Instant {
        val rules = targetZoneId.rules
        val ldt = LocalDateTime.of(date, time)
        val validOffsets = rules.getValidOffsets(ldt)

        return when {
            validOffsets.isEmpty() -> {
                // Spring forward gap
                val transition = rules.getTransition(ldt)
                if (policy == DstPolicy.NEXT_VALID_TIME && transition != null) {
                    transition.dateTimeAfter.atZone(targetZoneId).toInstant()
                } else {
                    // Fallback to standard transition instant
                    transition?.instant ?: ldt.atZone(targetZoneId).toInstant()
                }
            }
            validOffsets.size > 1 -> {
                // Fall back overlap: validOffsets[0] is usually earlier offset, [1] is later
                val selectedOffset = if (policy == DstPolicy.SECOND_OCCURRENCE) {
                    validOffsets.last()
                } else {
                    validOffsets.first()
                }
                ldt.atOffset(selectedOffset).toInstant()
            }
            else -> {
                ldt.atZone(targetZoneId).toInstant()
            }
        }
    }

    /**
     * Calculates the next absolute Instant when this alarm should ring.
     * Evaluates against target timezone rules so that recurrence stays anchored to target time.
     */
    fun calculateNextTriggerInstant(
        alarm: Alarm,
        referenceInstant: Instant = Instant.now()
    ): Instant? {
        if (!alarm.isEnabled) return null

        val targetZone = alarm.targetZoneId
        val targetNow = referenceInstant.atZone(targetZone)
        val targetLocalTime = alarm.localTime

        if (alarm.daysOfWeek.isEmpty()) {
            // One-time alarm
            var targetDate = targetNow.toLocalDate()
            val todayInstant = resolveInstant(targetZone, targetDate, targetLocalTime, alarm.dstPolicy)
            return if (todayInstant.isAfter(referenceInstant)) {
                todayInstant
            } else {
                targetDate = targetDate.plusDays(1)
                resolveInstant(targetZone, targetDate, targetLocalTime, alarm.dstPolicy)
            }
        } else {
            // Recurring alarm on specific days of the week (1=Mon ... 7=Sun)
            var candidateDate = targetNow.toLocalDate()
            for (dayOffset in 0..14) {
                val testDate = candidateDate.plusDays(dayOffset.toLong())
                val dayOfWeekValue = testDate.dayOfWeek.value
                if (alarm.daysOfWeek.contains(dayOfWeekValue)) {
                    val candidateInstant = resolveInstant(targetZone, testDate, targetLocalTime, alarm.dstPolicy)
                    if (candidateInstant.isAfter(referenceInstant)) {
                        return candidateInstant
                    }
                }
            }
            return null
        }
    }

    /**
     * Produces a live preview of when the phone will ring in device-local time.
     */
    fun previewLocalTime(
        alarm: Alarm,
        deviceZoneId: ZoneId = ZoneId.systemDefault(),
        is24Hour: Boolean = false,
        referenceInstant: Instant = Instant.now()
    ): LocalPreview {
        val nextInstant = calculateNextTriggerInstant(alarm, referenceInstant)
            ?: resolveInstant(alarm.targetZoneId, referenceInstant.atZone(alarm.targetZoneId).toLocalDate(), alarm.localTime, alarm.dstPolicy)

        val deviceZdt = nextInstant.atZone(deviceZoneId)
        val deviceLocalDate = deviceZdt.toLocalDate()
        val deviceToday = referenceInstant.atZone(deviceZoneId).toLocalDate()

        val timePattern = if (is24Hour) "HH:mm" else "h:mm"
        val formattedTime = deviceZdt.format(DateTimeFormatter.ofPattern(timePattern, Locale.getDefault()))
        val amPm = deviceZdt.format(DateTimeFormatter.ofPattern("a", Locale.getDefault())).uppercase()

        val dayDiff = deviceLocalDate.toEpochDay() - deviceToday.toEpochDay()
        val dayOffsetDescription = when (dayDiff) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            in 2L..6L -> deviceZdt.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
            else -> deviceLocalDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
        }

        return LocalPreview(
            deviceTime = formattedTime,
            deviceAmPm = amPm,
            dayOffsetDescription = dayOffsetDescription,
            instant = nextInstant
        )
    }

    /**
     * Formats human readable explanation for cross-timezone alarms.
     * e.g., "Rings at 9:00 AM Tokyo time (5:45 AM local)"
     */
    fun formatCrossTimezoneSummary(
        alarm: Alarm,
        deviceZoneId: ZoneId = ZoneId.systemDefault(),
        is24Hour: Boolean = false
    ): String {
        val preview = previewLocalTime(alarm, deviceZoneId, is24Hour)
        return if (alarm.isCrossTimezone) {
            "Rings at ${alarm.formattedTargetTime(is24Hour)} ${alarm.targetAmPm()} ${alarm.targetCityName} time (${preview.deviceTime} ${preview.deviceAmPm} local)"
        } else {
            "${preview.dayOffsetDescription} at ${alarm.formattedTargetTime(is24Hour)} ${alarm.targetAmPm()}"
        }
    }
}
