package com.example.chrona.feature.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.chrona.core.database.TimerDao
import com.example.chrona.core.model.TimerItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object TimerManager {
    const val CHANNEL_ID = "chrona_timer_channel"

    private val _timers = MutableStateFlow<List<TimerItem>>(emptyList())
    val timers: StateFlow<List<TimerItem>> = _timers.asStateFlow()

    private var timerDao: TimerDao? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickerJob: Job? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            timerDao = TimerDao(context)
            createNotificationChannel(context)

            scope.launch(Dispatchers.IO) {
                val persisted = timerDao!!.getAll()
                // Update deadline-based remaining times
                val updated = persisted.map { timer ->
                    if (timer.isRunning && timer.deadlineElapsedMillis > 0) {
                        val rem = (timer.deadlineElapsedMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                        timer.copy(
                            remainingMillis = rem,
                            isRunning = rem > 0,
                            isFinished = rem == 0L
                        )
                    } else {
                        timer
                    }
                }
                _timers.value = updated
                startTicker()
            }
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (true) {
                delay(200)
                var hasRunning = false
                val current = _timers.value.map { timer ->
                    if (timer.isRunning && timer.deadlineElapsedMillis > 0) {
                        val rem = (timer.deadlineElapsedMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                        if (rem == 0L && !timer.isFinished) {
                            notifyTimerComplete(timer)
                            timer.copy(remainingMillis = 0L, isRunning = false, isFinished = true)
                        } else {
                            hasRunning = true
                            timer.copy(remainingMillis = rem)
                        }
                    } else {
                        timer
                    }
                }
                _timers.value = current
                if (!hasRunning && current.all { !it.isRunning }) {
                    // All paused/stopped, reduce polling frequency
                    delay(800)
                }
            }
        }
    }

    fun addTimer(durationMillis: Long, label: String = "Timer") {
        val nowElapsed = SystemClock.elapsedRealtime()
        val timer = TimerItem(
            label = label,
            totalDurationMillis = durationMillis,
            remainingMillis = durationMillis,
            deadlineElapsedMillis = nowElapsed + durationMillis,
            isRunning = true,
            isFinished = false
        )
        val current = _timers.value.toMutableList()
        current.add(0, timer)
        _timers.value = current
        persist()
        startTicker()
    }

    fun togglePlayPause(timerId: String) {
        val nowElapsed = SystemClock.elapsedRealtime()
        val current = _timers.value.toMutableList()
        val index = current.indexOfFirst { it.id == timerId }
        if (index >= 0) {
            val t = current[index]
            if (t.isRunning) {
                // Pause: freeze remaining millis
                val rem = (t.deadlineElapsedMillis - nowElapsed).coerceAtLeast(0L)
                current[index] = t.copy(
                    remainingMillis = rem,
                    deadlineElapsedMillis = 0L,
                    isRunning = false
                )
            } else {
                // Resume: establish new deadline
                val rem = if (t.isFinished || t.remainingMillis <= 0) t.totalDurationMillis else t.remainingMillis
                current[index] = t.copy(
                    remainingMillis = rem,
                    deadlineElapsedMillis = nowElapsed + rem,
                    isRunning = true,
                    isFinished = false
                )
            }
            _timers.value = current
            persist()
            startTicker()
        }
    }

    fun addOneMinute(timerId: String) {
        val nowElapsed = SystemClock.elapsedRealtime()
        val current = _timers.value.toMutableList()
        val index = current.indexOfFirst { it.id == timerId }
        if (index >= 0) {
            val t = current[index]
            val extra = 60_000L
            val newTotal = t.totalDurationMillis + extra
            val newRem = t.remainingMillis + extra
            val newDeadline = if (t.isRunning) t.deadlineElapsedMillis + extra else nowElapsed + newRem
            current[index] = t.copy(
                totalDurationMillis = newTotal,
                remainingMillis = newRem,
                deadlineElapsedMillis = newDeadline,
                isFinished = false
            )
            _timers.value = current
            persist()
        }
    }

    fun resetTimer(timerId: String) {
        val current = _timers.value.toMutableList()
        val index = current.indexOfFirst { it.id == timerId }
        if (index >= 0) {
            val t = current[index]
            current[index] = t.copy(
                remainingMillis = t.totalDurationMillis,
                deadlineElapsedMillis = 0L,
                isRunning = false,
                isFinished = false
            )
            _timers.value = current
            persist()
        }
    }

    fun removeTimer(timerId: String) {
        val current = _timers.value.toMutableList()
        current.removeAll { it.id == timerId }
        _timers.value = current
        persist()
    }

    private fun persist() {
        val list = _timers.value
        scope.launch(Dispatchers.IO) {
            timerDao?.saveAll(list)
        }
    }

    private fun notifyTimerComplete(timer: TimerItem) {
        val context = appContext ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Timer Finished")
            .setContentText("${timer.label} has reached 0:00")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(timer.id.hashCode(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Chrona Timers",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when timers finish"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
