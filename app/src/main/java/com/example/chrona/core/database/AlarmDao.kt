package com.example.chrona.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.model.DstPolicy

class AlarmDao(context: Context) {
    private val dbHelper = ChronaDatabaseHelper.getInstance(context)

    fun getAll(): List<Alarm> {
        val list = mutableListOf<Alarm>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "alarms",
            null,
            null,
            null,
            null,
            null,
            "hour ASC, minute ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToAlarm(it))
            }
        }
        return list
    }

    fun getById(id: String): Alarm? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "alarms",
            null,
            "id = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return mapCursorToAlarm(it)
            }
        }
        return null
    }

    fun insertOrReplace(alarm: Alarm) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("id", alarm.id)
            put("label", alarm.label)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("target_zone_id", alarm.targetZoneIdString)
            put("target_city_name", alarm.targetCityName)
            put("target_country_name", alarm.targetCountryName)
            put("identity_color_hex", alarm.identityColorHex)
            put("days_of_week", alarm.daysOfWeek.joinToString(","))
            put("is_enabled", if (alarm.isEnabled) 1 else 0)
            put("vibrate", if (alarm.vibrate) 1 else 0)
            put("sound_uri", alarm.soundUri)
            put("snooze_duration_minutes", alarm.snoozeDurationMinutes)
            put("gradual_volume_ramp", if (alarm.gradualVolumeRamp) 1 else 0)
            put("dst_policy", alarm.dstPolicy.name)
            put("created_at", alarm.createdAtEpochMillis)
        }
        db.insertWithOnConflict("alarms", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun delete(id: String) {
        val db = dbHelper.writableDatabase
        db.delete("alarms", "id = ?", arrayOf(id))
    }

    private fun mapCursorToAlarm(cursor: Cursor): Alarm {
        val daysString = cursor.getString(cursor.getColumnIndexOrThrow("days_of_week"))
        val daysSet = if (daysString.isBlank()) {
            emptySet()
        } else {
            daysString.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }

        val dstPolicyStr = cursor.getString(cursor.getColumnIndexOrThrow("dst_policy"))
        val dstPolicy = try {
            DstPolicy.valueOf(dstPolicyStr)
        } catch (e: Exception) {
            DstPolicy.NEXT_VALID_TIME
        }

        return Alarm(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            label = cursor.getString(cursor.getColumnIndexOrThrow("label")),
            hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour")),
            minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute")),
            targetZoneIdString = cursor.getString(cursor.getColumnIndexOrThrow("target_zone_id")),
            targetCityName = cursor.getString(cursor.getColumnIndexOrThrow("target_city_name")),
            targetCountryName = cursor.getString(cursor.getColumnIndexOrThrow("target_country_name")),
            identityColorHex = cursor.getString(cursor.getColumnIndexOrThrow("identity_color_hex")),
            daysOfWeek = daysSet,
            isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("is_enabled")) == 1,
            vibrate = cursor.getInt(cursor.getColumnIndexOrThrow("vibrate")) == 1,
            soundUri = cursor.getString(cursor.getColumnIndexOrThrow("sound_uri")),
            snoozeDurationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("snooze_duration_minutes")),
            gradualVolumeRamp = cursor.getInt(cursor.getColumnIndexOrThrow("gradual_volume_ramp")) == 1,
            dstPolicy = dstPolicy,
            createdAtEpochMillis = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }
}
