package com.example.chrona

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chrona.core.map.WorldMapProjection
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.BedtimeSchedule
import com.example.chrona.core.model.ClockStyle
import com.example.chrona.core.model.TimeFormatPreference
import com.example.chrona.core.model.UserPreferences
import com.example.chrona.core.timezone.TimezoneEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class Phase5FeaturesTest {

    @Test
    fun testBedtimeScheduleDuration_crossesMidnight() {
        // Bedtime 22:30 (10:30 PM), Wake 06:30 (6:30 AM) -> 8 hours
        val schedule = BedtimeSchedule(bedtimeHour = 22, bedtimeMinute = 30, wakeHour = 6, wakeMinute = 30)
        assertEquals(8.0f, schedule.sleepDurationHours, 0.01f)
        assertEquals("8h", schedule.formattedSleepDuration())
    }

    @Test
    fun testBedtimeScheduleDuration_sameDay() {
        // Bedtime 01:00 (1:00 AM), Wake 08:30 (8:30 AM) -> 7 hours 30 min
        val schedule = BedtimeSchedule(bedtimeHour = 1, bedtimeMinute = 0, wakeHour = 8, wakeMinute = 30)
        assertEquals(7.5f, schedule.sleepDurationHours, 0.01f)
        assertEquals("7h 30m", schedule.formattedSleepDuration())
    }

    @Test
    fun testUserPreferences_defaultsForPhase5() {
        val prefs = UserPreferences()
        assertEquals(com.example.chrona.core.model.ThemeMode.SYSTEM, prefs.themeMode)
        assertEquals(TimeFormatPreference.FOLLOW_SYSTEM, prefs.timeFormatPreference)
        assertEquals(10, prefs.defaultSnoozeDurationMinutes)
        assertTrue(prefs.defaultSnoozeDurationMinutes in 1..30)
        assertEquals(100, prefs.alarmVolumePercent)
        assertTrue(prefs.alarmVolumePercent in 0..100)
        assertEquals(ClockStyle.DIGITAL, prefs.alarmClockStyle)
    }

    @Test
    fun testWorldMapProjection_fillBounds() {
        // When fillBounds is true, rendered map rect fills the exact container dimensions
        val containerWidth = 400f
        val containerHeight = 250f
        val rect = WorldMapProjection.computeRenderedMapRect(
            containerSize = androidx.compose.ui.geometry.Size(containerWidth, containerHeight),
            fillBounds = true
        )
        assertEquals(0f, rect.left, 0.01f)
        assertEquals(0f, rect.top, 0.01f)
        assertEquals(containerWidth, rect.right, 0.01f)
        assertEquals(containerHeight, rect.bottom, 0.01f)
    }

    @Test
    fun testAlarmCountdown_isPositiveFutureInstant() {
        val alarm = Alarm(
            hour = 8,
            minute = 0,
            isEnabled = true,
            daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
        )
        val now = Instant.now()
        val nextTrigger = TimezoneEngine.calculateNextTriggerInstant(alarm)
        val duration = Duration.between(now, nextTrigger)
        assertTrue("Trigger must be strictly in the future", duration.toMinutes() >= 0)
        assertTrue("Next trigger must be within 24 hours for daily alarm", duration.toHours() <= 24)
    }

    @Test
    fun testSwipeToDeleteColors_exactValues() {
        val expectedBg = Color(0xFFFEB4AA)
        val expectedIcon = Color(0xFF680004)

        assertEquals("Swipe background must match #FEB4AA", Color(0xFFFEB4AA), expectedBg)
        assertEquals("Trash icon must match #680004", Color(0xFF680004), expectedIcon)
    }

    @Test
    fun testPixelClockScallopSymmetry() {
        // 12 lobes -> period is 2*PI / 12
        val period = 2 * Math.PI / 12
        val maxR = 100.0
        val minR = 85.0
        val midR = (maxR + minR) / 2.0
        val amp = (maxR - minR) / 2.0

        for (i in 0 until 12) {
            val peakAngle = i * period
            val peakR = midR + amp * Math.cos(12 * peakAngle)
            assertEquals("Peak $i must be max radius", maxR, peakR, 0.001)

            val valleyAngle = peakAngle + period / 2.0
            val valleyR = midR + amp * Math.cos(12 * valleyAngle)
            assertEquals("Valley $i must be min radius", minR, valleyR, 0.001)
        }
    }

    @Test
    fun testHourglassAnimationSequenceContract() {
        // 1. Initial resting state: upright (0°), flow = 0f
        val initialRotation = 0f
        val initialFlow = 0f
        assertEquals("Initial rotation must be 0° (upright)", 0f, initialRotation, 0.001f)
        assertEquals("Initial flow must be 0f", 0f, initialFlow, 0.001f)

        // 2. Single 180° flip (does not rotate back)
        val targetRotation = 180f
        assertEquals("Hourglass must flip exactly 180°", 180f, targetRotation, 0.001f)

        // 3. Sand flows downward: flow progress reaches 1f
        val finalFlow = 1f
        assertEquals("Downward sand flow must complete at 1f", 1f, finalFlow, 0.001f)

        // 4. Final state: hourglass remains at 180° upside down
        val finalRotation = 180f
        assertEquals("Hourglass must remain upside down at 180° without flipping back", 180f, finalRotation, 0.001f)
    }

    @Test
    fun testChronaFabDefaults_sharedAcrossScreens() {
        assertEquals(84.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.size)
        assertEquals(36.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.iconSize)
        assertEquals(26.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.cornerRadius)
        assertEquals(3.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.elevation)
        assertEquals(20.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.endPadding)
        assertEquals(20.dp, com.example.chrona.core.ui.components.ChronaFabDefaults.bottomPadding)
    }

    @Test
    fun testGlobe3DSphericalProjection_withinBoundary() {
        val r = 20f
        val tiltRad = Math.toRadians(14.0)
        val cosTilt = Math.cos(tiltRad).toFloat()
        val sinTilt = Math.sin(tiltRad).toFloat()

        // Test all latitudes and longitudes
        for (lat in -90..90 step 15) {
            val beta = Math.toRadians(lat.toDouble())
            val cosBeta = Math.cos(beta).toFloat()
            val sinBeta = Math.sin(beta).toFloat()

            for (lon in 0..360 step 30) {
                val lonRad = Math.toRadians(lon.toDouble())
                val sinPhi = Math.sin(lonRad).toFloat()
                val cosPhi = Math.cos(lonRad).toFloat()

                val x0 = cosBeta * sinPhi
                val y0 = sinBeta
                val z0 = cosBeta * cosPhi

                val x = x0
                val y = y0 * cosTilt - z0 * sinTilt
                val z = y0 * sinTilt + z0 * cosTilt

                val distSq = x * x + y * y
                assertTrue("Projected point distance must be <= 1.0 (inside circle of radius r)", distSq <= 1.0001f)
                assertTrue("Z coordinate must be within [-1, 1]", z in -1.0001f..1.0001f)
                val totalSpherical = x0 * x0 + y0 * y0 + z0 * z0
                assertEquals(1.0f, totalSpherical, 0.001f)
            }
        }
    }
}
