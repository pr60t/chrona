package com.example.chrona.feature.stopwatch

import android.content.Context
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
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            StopwatchNotificationManager.createChannel(context.applicationContext)
        }
    }

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
            appContext?.let {
                StopwatchNotificationManager.updateNotification(it)
                com.example.chrona.widget.WidgetUpdateHelper.updateStopwatchWidgets(it)
            }
        }
    }

    fun pause() {
        if (_isRunning.value) {
            accumulatedMillis += (clock() - baseTimeElapsed)
            _isRunning.value = false
            appContext?.let {
                StopwatchNotificationManager.updateNotification(it)
                com.example.chrona.widget.WidgetUpdateHelper.updateStopwatchWidgets(it)
            }
        }
    }

    fun reset() {
        _isRunning.value = false
        accumulatedMillis = 0L
        baseTimeElapsed = 0L
        lastLapTotalTime = 0L
        _laps.value = emptyList()
        appContext?.let {
            StopwatchNotificationManager.updateNotification(it)
            com.example.chrona.widget.WidgetUpdateHelper.updateStopwatchWidgets(it)
        }
    }

    fun recordLap() {
        val total = getElapsedMillis()
        val lapTime = total - lastLapTotalTime
        lastLapTotalTime = total
        val current = _laps.value.toMutableList()
        current.add(0, Lap(current.size + 1, lapTime, total))
        _laps.value = current
        appContext?.let {
            StopwatchNotificationManager.updateNotification(it)
            com.example.chrona.widget.WidgetUpdateHelper.updateStopwatchWidgets(it)
        }
    }
}
