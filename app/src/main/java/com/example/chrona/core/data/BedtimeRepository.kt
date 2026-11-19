package com.example.chrona.core.data

import com.example.chrona.core.model.BedtimeSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BedtimeRepository {
    private val _schedule = MutableStateFlow(BedtimeSchedule())
    val schedule: StateFlow<BedtimeSchedule> = _schedule.asStateFlow()

    fun updateSchedule(newSchedule: BedtimeSchedule) {
        _schedule.value = newSchedule
    }

    fun toggleEnabled() {
        _schedule.value = _schedule.value.copy(isEnabled = !_schedule.value.isEnabled)
    }
}
