package com.example.chrona

import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.DstPolicy
import com.example.chrona.core.timezone.TimezoneEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class TimezoneEngineTest {

    @Test
    fun testTokyoKathmanduCrossTimezoneCalculation() {
        // Tokyo (UTC+9) and Kathmandu (UTC+5:45)
        // 9:00 AM Tokyo = 05:45 AM Kathmandu
        val tokyoZone = ZoneId.of("Asia/Tokyo")
        val kathmanduZone = ZoneId.of("Asia/Kathmandu")

        val alarm = Alarm(
            hour = 9,
            minute = 0,
            targetZoneIdString = "Asia/Tokyo",
            targetCityName = "Tokyo",
            daysOfWeek = emptySet(), // one-time
            isEnabled = true
        )

        // Reference: 2026-11-19 00:00 UTC (09:00 Tokyo, 05:45 Kathmandu)
        val reference = LocalDateTime.of(2026, 11, 19, 3, 0)
            .atZone(ZoneOffset.UTC).toInstant() // 12:00 Tokyo

        val triggerInstant = TimezoneEngine.calculateNextTriggerInstant(alarm, reference)
        assertNotNull(triggerInstant)

        // Trigger should be next day at 9:00 AM Tokyo
        val triggerTokyo = triggerInstant!!.atZone(tokyoZone)
        assertEquals(9, triggerTokyo.hour)
        assertEquals(0, triggerTokyo.minute)
        assertEquals(20, triggerTokyo.dayOfMonth)

        // Verify device-local time in Kathmandu
        val triggerKathmandu = triggerInstant.atZone(kathmanduZone)
        assertEquals(5, triggerKathmandu.hour)
        assertEquals(45, triggerKathmandu.minute)
        assertEquals(20, triggerKathmandu.dayOfMonth)
    }

    @Test
    fun testDstSpringForwardGapDetectionAndResolution() {
        // America/New_York on March 8, 2026: clocks jump from 02:00 EST to 03:00 EDT
        // 02:30 does not exist
        val nyZone = ZoneId.of("America/New_York")
        val gapDate = LocalDate.of(2026, 3, 8)
        val gapTime = LocalTime.of(2, 30)

        val dstStatus = TimezoneEngine.checkDstStatus(nyZone, gapDate, gapTime)
        assertTrue("Expected DST gap for 2:30 AM on spring-forward", dstStatus.isGap)
        assertFalse("Should not be an overlap", dstStatus.isOverlap)
        assertEquals(LocalTime.of(3, 30), dstStatus.suggestedTime)

        // Resolution under NEXT_VALID_TIME
        val resolvedInstant = TimezoneEngine.resolveInstant(
            nyZone,
            gapDate,
            gapTime,
            DstPolicy.NEXT_VALID_TIME
        )
        val resolvedZdt = resolvedInstant.atZone(nyZone)
        assertEquals(3, resolvedZdt.hour) // pushed to 3:00 or after
    }

    @Test
    fun testDstFallBackOverlapDetection() {
        // America/New_York on November 1, 2026: clocks jump from 02:00 EDT back to 01:00 EST
        // 01:30 occurs twice
        val nyZone = ZoneId.of("America/New_York")
        val overlapDate = LocalDate.of(2026, 11, 1)
        val overlapTime = LocalTime.of(1, 30)

        val dstStatus = TimezoneEngine.checkDstStatus(nyZone, overlapDate, overlapTime)
        assertFalse("Should not be a gap", dstStatus.isGap)
        assertTrue("Expected DST overlap for 1:30 AM on fall-back", dstStatus.isOverlap)

        // Test FIRST_OCCURRENCE vs SECOND_OCCURRENCE
        val firstInstant = TimezoneEngine.resolveInstant(
            nyZone,
            overlapDate,
            overlapTime,
            DstPolicy.FIRST_OCCURRENCE
        )
        val secondInstant = TimezoneEngine.resolveInstant(
            nyZone,
            overlapDate,
            overlapTime,
            DstPolicy.SECOND_OCCURRENCE
        )

        // The first occurrence is 1 hour before the second occurrence!
        val duration = Duration.between(firstInstant, secondInstant)
        assertEquals(3600L, duration.seconds)
    }

    @Test
    fun testRecurringAlarmAdvance() {
        val alarm = Alarm(
            hour = 8,
            minute = 0,
            targetZoneIdString = "SYSTEM",
            daysOfWeek = setOf(1, 3, 5), // Mon, Wed, Fri
            isEnabled = true
        )

        // Reference: Monday 8:30 AM
        val monday830 = LocalDateTime.of(2026, 11, 16, 8, 30)
            .atZone(ZoneId.systemDefault()).toInstant()

        val nextTrigger = TimezoneEngine.calculateNextTriggerInstant(alarm, monday830)
        assertNotNull(nextTrigger)

        val nextZdt = nextTrigger!!.atZone(ZoneId.systemDefault())
        assertEquals(DayOfWeek.WEDNESDAY, nextZdt.dayOfWeek)
        assertEquals(8, nextZdt.hour)
        assertEquals(0, nextZdt.minute)
    }
}
