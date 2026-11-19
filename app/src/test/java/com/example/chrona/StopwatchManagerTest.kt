package com.example.chrona

import com.example.chrona.feature.stopwatch.StopwatchManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StopwatchManagerTest {

    private var fakeTimeMillis: Long = 1000L

    @Before
    fun setUp() {
        fakeTimeMillis = 1000L
        StopwatchManager.clock = { fakeTimeMillis }
        StopwatchManager.reset()
    }

    @After
    fun tearDown() {
        StopwatchManager.reset()
    }

    @Test
    fun testInitialStateIsStoppedAndZero() {
        assertFalse(StopwatchManager.isRunning.value)
        assertEquals(0L, StopwatchManager.getElapsedMillis())
        assertTrue(StopwatchManager.laps.value.isEmpty())
    }

    @Test
    fun testStartIncrementsElapsedMonotonically() {
        StopwatchManager.start()
        assertTrue(StopwatchManager.isRunning.value)

        fakeTimeMillis += 2500L
        assertEquals(2500L, StopwatchManager.getElapsedMillis())

        fakeTimeMillis += 1500L
        assertEquals(4000L, StopwatchManager.getElapsedMillis())
    }

    @Test
    fun testPauseFreezesAccumulatedTime() {
        StopwatchManager.start()
        fakeTimeMillis += 3000L
        StopwatchManager.pause()

        assertFalse(StopwatchManager.isRunning.value)
        assertEquals(3000L, StopwatchManager.getElapsedMillis())

        // Time elapses while paused
        fakeTimeMillis += 5000L
        assertEquals(3000L, StopwatchManager.getElapsedMillis())
    }

    @Test
    fun testResumeContinuesAccumulating() {
        StopwatchManager.start()
        fakeTimeMillis += 2000L
        StopwatchManager.pause()
        fakeTimeMillis += 10000L // paused interval

        StopwatchManager.start()
        fakeTimeMillis += 1500L
        assertEquals(3500L, StopwatchManager.getElapsedMillis())
    }

    @Test
    fun testRecordLapGeneratesCorrectSplitsAndTotal() {
        StopwatchManager.start()
        fakeTimeMillis += 2000L
        StopwatchManager.recordLap() // Lap 1: 2000ms, total: 2000ms

        fakeTimeMillis += 3000L
        StopwatchManager.recordLap() // Lap 2: 3000ms, total: 5000ms

        val laps = StopwatchManager.laps.value
        assertEquals(2, laps.size)

        // Laps are stored newest first
        val latestLap = laps[0]
        assertEquals(2, latestLap.lapIndex)
        assertEquals(3000L, latestLap.lapTimeMillis)
        assertEquals(5000L, latestLap.totalTimeMillis)

        val firstLap = laps[1]
        assertEquals(1, firstLap.lapIndex)
        assertEquals(2000L, firstLap.lapTimeMillis)
        assertEquals(2000L, firstLap.totalTimeMillis)
    }

    @Test
    fun testResetClearsAllLapsAndElapsed() {
        StopwatchManager.start()
        fakeTimeMillis += 4000L
        StopwatchManager.recordLap()
        StopwatchManager.reset()

        assertFalse(StopwatchManager.isRunning.value)
        assertEquals(0L, StopwatchManager.getElapsedMillis())
        assertTrue(StopwatchManager.laps.value.isEmpty())
    }
}
