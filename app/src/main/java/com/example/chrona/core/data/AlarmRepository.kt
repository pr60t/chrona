package com.example.chrona.core.data

import android.content.Context
import com.example.chrona.core.alarm.AlarmScheduler
import com.example.chrona.core.database.AlarmDao
import com.example.chrona.core.model.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AlarmRepository {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _ringingAlarm = MutableStateFlow<Alarm?>(null)
    val ringingAlarm: StateFlow<Alarm?> = _ringingAlarm.asStateFlow()

    private var alarmDao: AlarmDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        if (alarmDao == null) {
            alarmDao = AlarmDao(context)
            scope.launch {
                val loaded = alarmDao!!.getAll()
                _alarms.value = loaded
            }
        }
    }

    fun getAlarmById(context: Context, id: String): Alarm? {
        // First check in-memory state, fallback to direct DB read
        return _alarms.value.find { it.id == id } ?: AlarmDao(context).getById(id)
    }

    fun setRingingAlarm(alarm: Alarm?) {
        _ringingAlarm.value = alarm
    }

    fun saveAlarm(context: Context, alarm: Alarm) {
        val dao = alarmDao ?: AlarmDao(context)
        val current = _alarms.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index >= 0) {
            current[index] = alarm
        } else {
            current.add(0, alarm)
        }
        _alarms.value = current

        scope.launch {
            dao.insertOrReplace(alarm)
        }

        if (alarm.isEnabled) {
            AlarmScheduler.scheduleAlarm(context, alarm)
        } else {
            AlarmScheduler.cancelAlarm(context, alarm)
        }
        notifyWidgets(context)
    }

    private fun notifyWidgets(context: Context) {
        try {
            com.example.chrona.widget.WidgetUpdateHelper.updateDigitalWidgets(context)
            com.example.chrona.widget.WidgetUpdateHelper.updateStackedWidgets(context)
        } catch (_: Exception) {}
    }

    fun toggleAlarm(context: Context, alarmId: String) {
        val dao = alarmDao ?: AlarmDao(context)
        val current = _alarms.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarmId }
        if (index >= 0) {
            val updated = current[index].copy(isEnabled = !current[index].isEnabled)
            current[index] = updated
            _alarms.value = current

            scope.launch {
                dao.insertOrReplace(updated)
            }

            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, updated)
            } else {
                AlarmScheduler.cancelAlarm(context, updated)
            }
            notifyWidgets(context)
        }
    }

    fun deleteAlarm(context: Context, alarmId: String) {
        val dao = alarmDao ?: AlarmDao(context)
        val current = _alarms.value.toMutableList()
        val target = current.find { it.id == alarmId }
        if (target != null) {
            AlarmScheduler.cancelAlarm(context, target)
            current.remove(target)
            _alarms.value = current

            scope.launch {
                dao.delete(alarmId)
            }
            notifyWidgets(context)
        }
    }

    fun snoozeAlarm(context: Context, alarmId: String) {
        val actualId = com.example.chrona.core.alarm.AlarmScheduler.getOriginalAlarmId(alarmId)
        val alarm = getAlarmById(context, actualId) ?: return
        val snoozeMinutes = alarm.snoozeDurationMinutes.coerceAtLeast(1)
        val snoozeAlarm = alarm.copy(
            id = actualId,
            label = "Snoozed (${snoozeMinutes}m): ${alarm.label}",
            isEnabled = true,
            daysOfWeek = emptySet()
        )
        com.example.chrona.core.alarm.AlarmScheduler.scheduleSnooze(context, snoozeAlarm, snoozeMinutes)
        com.example.chrona.core.alarm.SnoozeNotificationManager.showSnoozeNotification(context, alarm, snoozeMinutes)
    }

    fun rescheduleAll(context: Context) {
        val dao = alarmDao ?: AlarmDao(context)
        scope.launch {
            val loaded = dao.getAll()
            _alarms.value = loaded
            loaded.filter { it.isEnabled }.forEach { alarm ->
                AlarmScheduler.scheduleAlarm(context, alarm)
            }
        }
    }
}
