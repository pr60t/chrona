package com.example.chrona.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class SavedCityDao(context: Context) {
    private val dbHelper = ChronaDatabaseHelper.getInstance(context)

    fun getSavedCityIds(): Set<Long> {
        val set = mutableSetOf<Long>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "saved_cities",
            arrayOf("city_id"),
            null,
            null,
            null,
            null,
            "saved_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                set.add(it.getLong(0))
            }
        }
        return set
    }

    fun add(cityId: Long) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("city_id", cityId)
            put("saved_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("saved_cities", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun remove(cityId: Long) {
        val db = dbHelper.writableDatabase
        db.delete("saved_cities", "city_id = ?", arrayOf(cityId.toString()))
    }

    fun replaceAll(ids: List<Long>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("saved_cities", null, null)
            val baseTime = System.currentTimeMillis()
            ids.forEachIndexed { index, cityId ->
                val cv = ContentValues().apply {
                    put("city_id", cityId)
                    put("saved_at", baseTime + index * 1000L)
                }
                db.insertWithOnConflict("saved_cities", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
