package com.example.chrona.feature.stopwatch

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Lap(
    val lapIndex: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long
)

object StopwatchManager {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var baseTimeElapsed: Long = 0L
    private var accumulatedMillis: Long = 0L

    private val _laps = MutableStateFlow<List<Lap>>(emptyList())
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private var lastLapTotalTime: Long = 0L

    internal var clock: () -> Long = { SystemClock.elapsedRealtime() }

    fun getElapsedMillis(): Long {
        return if (_isRunning.value) {
            accumulatedMillis + (clock() - baseTimeElapsed)
        } else {
            accumulatedMillis
        }
    }

    fun start() {
        if (!_isRunning.value) {
            baseTimeElapsed = clock()
            _isRunning.value = true
        }
    }

    fun pause() {
        if (_isRunning.value) {
            accumulatedMillis += (clock() - baseTimeElapsed)
            _isRunning.value = false
        }
    }

    fun reset() {
        _isRunning.value = false
        accumulatedMillis = 0L
        baseTimeElapsed = 0L
        lastLapTotalTime = 0L
        _laps.value = emptyList()
    }

    fun recordLap() {
        val total = getElapsedMillis()
        val lapTime = total - lastLapTotalTime
        lastLapTotalTime = total
        val current = _laps.value.toMutableList()
        current.add(0, Lap(current.size + 1, lapTime, total))
        _laps.value = current
    }
}
