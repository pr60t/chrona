package com.example.chrona

import com.example.chrona.core.alarm.AlarmScheduler
import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeLogicTest {

    @Test
    fun testSnoozeIdGeneration() {
        val originalId = "alarm_12345"
        val snoozeId = AlarmScheduler.getSnoozeId(originalId)
        assertEquals("snooze_alarm_12345", snoozeId)

        // Idempotency: calling getSnoozeId on an existing snoozeId does not double-prefix
        val doubleSnoozeId = AlarmScheduler.getSnoozeId(snoozeId)
        assertEquals("snooze_alarm_12345", doubleSnoozeId)
    }

    @Test
    fun testOriginalAlarmIdExtraction() {
        val snoozeId = "snooze_alarm_98765"
        val extractedId = AlarmScheduler.getOriginalAlarmId(snoozeId)
        assertEquals("alarm_98765", extractedId)

        // Calling on a non-snooze id returns the id as-is
        val plainId = "normal_alarm"
        assertEquals("normal_alarm", AlarmScheduler.getOriginalAlarmId(plainId))
    }
}
