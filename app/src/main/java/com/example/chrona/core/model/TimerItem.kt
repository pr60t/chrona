package com.example.chrona.core.model

import android.os.SystemClock
import java.util.UUID

data class TimerItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Timer",
    val totalDurationMillis: Long,
    val remainingMillis: Long = totalDurationMillis,
    val deadlineElapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
) {
    val progress: Float
        get() = if (totalDurationMillis > 0) {
            (currentRemainingMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)
        } else 0f

    val currentRemainingMillis: Long
        get() = if (isRunning && deadlineElapsedMillis > 0) {
            (deadlineElapsedMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        } else {
            remainingMillis
        }

    fun formattedRemaining(): String {
        val rem = currentRemainingMillis
        val totalSecs = (rem + 999) / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}
