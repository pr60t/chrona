package com.example.chrona.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.chrona.core.model.TimerItem

class TimerDao(context: Context) {
    private val dbHelper = ChronaDatabaseHelper.getInstance(context)

    fun getAll(): List<TimerItem> {
        val list = mutableListOf<TimerItem>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "active_timers",
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TimerItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        label = it.getString(it.getColumnIndexOrThrow("label")),
                        totalDurationMillis = it.getLong(it.getColumnIndexOrThrow("total_duration_millis")),
                        remainingMillis = it.getLong(it.getColumnIndexOrThrow("remaining_millis")),
                        deadlineElapsedMillis = it.getLong(it.getColumnIndexOrThrow("deadline_elapsed_millis")),
                        isRunning = it.getInt(it.getColumnIndexOrThrow("is_running")) == 1,
                        isFinished = it.getInt(it.getColumnIndexOrThrow("is_finished")) == 1
                    )
                )
            }
        }
        return list
    }

    fun saveAll(timers: List<TimerItem>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("active_timers", null, null)
            timers.forEach { timer ->
                val cv = ContentValues().apply {
                    put("id", timer.id)
                    put("label", timer.label)
                    put("total_duration_millis", timer.totalDurationMillis)
                    put("remaining_millis", timer.remainingMillis)
                    put("deadline_elapsed_millis", timer.deadlineElapsedMillis)
                    put("is_running", if (timer.isRunning) 1 else 0)
                    put("is_finished", if (timer.isFinished) 1 else 0)
                }
                db.insertWithOnConflict("active_timers", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun delete(id: String) {
        val db = dbHelper.writableDatabase
        db.delete("active_timers", "id = ?", arrayOf(id))
    }
}
