package com.example.chrona.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ChronaDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "chrona.db"
        const val DATABASE_VERSION = 1

        private var instance: ChronaDatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): ChronaDatabaseHelper {
            if (instance == null) {
                instance = ChronaDatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Alarms table
        db.execSQL(
            """
            CREATE TABLE alarms (
                id TEXT PRIMARY KEY,
                label TEXT NOT NULL,
                hour INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                target_zone_id TEXT NOT NULL,
                target_city_name TEXT NOT NULL,
                target_country_name TEXT NOT NULL,
                identity_color_hex TEXT NOT NULL,
                days_of_week TEXT NOT NULL,
                is_enabled INTEGER NOT NULL,
                vibrate INTEGER NOT NULL,
                sound_uri TEXT,
                snooze_duration_minutes INTEGER NOT NULL DEFAULT 10,
                gradual_volume_ramp INTEGER NOT NULL DEFAULT 1,
                dst_policy TEXT NOT NULL DEFAULT 'NEXT_VALID_TIME',
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_alarms_enabled ON alarms(is_enabled)")

        // Saved cities table
        db.execSQL(
            """
            CREATE TABLE saved_cities (
                city_id INTEGER PRIMARY KEY,
                saved_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Active timers table
        db.execSQL(
            """
            CREATE TABLE active_timers (
                id TEXT PRIMARY KEY,
                label TEXT NOT NULL,
                total_duration_millis INTEGER NOT NULL,
                remaining_millis INTEGER NOT NULL,
                deadline_elapsed_millis INTEGER NOT NULL,
                is_running INTEGER NOT NULL,
                is_finished INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future schema migrations
    }
}
