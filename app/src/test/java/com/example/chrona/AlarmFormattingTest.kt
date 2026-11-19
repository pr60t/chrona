package com.example.chrona

import com.example.chrona.core.model.Alarm
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmFormattingTest {

    @Test
    fun test12HourVs24HourFormatting() {
        val midnightAlarm = Alarm(hour = 0, minute = 5)
        assertEquals("12:05", midnightAlarm.formattedTargetTime(is24Hour = false))
        assertEquals("AM", midnightAlarm.targetAmPm())
        assertEquals("00:05", midnightAlarm.formattedTargetTime(is24Hour = true))

        val noonAlarm = Alarm(hour = 12, minute = 0)
        assertEquals("12:00", noonAlarm.formattedTargetTime(is24Hour = false))
        assertEquals("PM", noonAlarm.targetAmPm())
        assertEquals("12:00", noonAlarm.formattedTargetTime(is24Hour = true))

        val eveningAlarm = Alarm(hour = 21, minute = 45)
        assertEquals("9:45", eveningAlarm.formattedTargetTime(is24Hour = false))
        assertEquals("PM", eveningAlarm.targetAmPm())
        assertEquals("21:45", eveningAlarm.formattedTargetTime(is24Hour = true))
    }

    @Test
    fun testRepeatSummaryStrings() {
        val onceAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = emptySet())
        assertEquals("Once", onceAlarm.repeatSummary())

        val dailyAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7))
        assertEquals("Every day", dailyAlarm.repeatSummary())

        val weekdayAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = setOf(1, 2, 3, 4, 5))
        assertEquals("Weekdays", weekdayAlarm.repeatSummary())

        val weekendAlarm = Alarm(hour = 9, minute = 0, daysOfWeek = setOf(6, 7))
        assertEquals("Weekends", weekendAlarm.repeatSummary())

        val customAlarm = Alarm(hour = 7, minute = 0, daysOfWeek = setOf(1, 3, 5))
        assertEquals("Mon Wed Fri", customAlarm.repeatSummary())
    }

    @Test
    fun testIdentityColorParsing() {
        val alarmWithHex = Alarm(hour = 8, minute = 0, identityColorHex = "#A7E8D8")
        val color = alarmWithHex.identityColor
        // Alpha should be 1.0 (255)
        assertEquals(1.0f, color.alpha, 0.01f)
    }
}
