package com.example.chrona.core.data

import android.content.Context
import android.content.SharedPreferences
import com.example.chrona.core.model.BedtimeSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BedtimeRepository {
    private const val PREFS_NAME = "chrona_bedtime_prefs"
    private const val KEY_BEDTIME_H = "bedtime_h"
    private const val KEY_BEDTIME_M = "bedtime_m"
    private const val KEY_WAKE_H = "wake_h"
    private const val KEY_WAKE_M = "wake_m"
    private const val KEY_ENABLED = "enabled"

    private var prefs: SharedPreferences? = null

    private val _schedule = MutableStateFlow(BedtimeSchedule())
    val schedule: StateFlow<BedtimeSchedule> = _schedule.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        val bH = sp.getInt(KEY_BEDTIME_H, 22)
        val bM = sp.getInt(KEY_BEDTIME_M, 30)
        val wH = sp.getInt(KEY_WAKE_H, 6)
        val wM = sp.getInt(KEY_WAKE_M, 30)
        val enabled = sp.getBoolean(KEY_ENABLED, true)

        _schedule.value = BedtimeSchedule(
            bedtimeHour = bH,
            bedtimeMinute = bM,
            wakeHour = wH,
            wakeMinute = wM,
            isEnabled = enabled
        )
    }

    fun updateSchedule(newSchedule: BedtimeSchedule) {
        _schedule.value = newSchedule
        prefs?.edit()?.apply {
            putInt(KEY_BEDTIME_H, newSchedule.bedtimeHour)
            putInt(KEY_BEDTIME_M, newSchedule.bedtimeMinute)
            putInt(KEY_WAKE_H, newSchedule.wakeHour)
            putInt(KEY_WAKE_M, newSchedule.wakeMinute)
            putBoolean(KEY_ENABLED, newSchedule.isEnabled)
            apply()
        }
    }

    fun toggleEnabled() {
        val updated = _schedule.value.copy(isEnabled = !_schedule.value.isEnabled)
        updateSchedule(updated)
    }
}
