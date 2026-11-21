package com.example.chrona.core.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.chrona.core.model.City
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer

/**
 * Access layer for the prebuilt offline global city catalogue SQLite database.
 * Contains 237,321 verified cities with exact coordinates, IANA timezones, and normalized aliases.
 */
class CityCatalogueDatabase private constructor(
    private val dbFile: File,
    private val assetManager: android.content.res.AssetManager
) {

    companion object {
        private const val DB_NAME = "cities.db"
        private const val ASSET_PATH = "databases/cities.db"
        private const val MIN_VALID_DB_SIZE = 10_000_000L // ~58MB expected

        @Volatile
        private var instance: CityCatalogueDatabase? = null

        fun getInstance(context: Context): CityCatalogueDatabase {
            val appCtx = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: CityCatalogueDatabase(
                    appCtx.getDatabasePath(DB_NAME),
                    appCtx.assets
                ).also { instance = it }
            }
        }

        fun normalizeText(text: String): String {
            val pre = text.trim()
                .replace('ł', 'l')
                .replace('Ł', 'l')
                .replace('ø', 'o')
                .replace('Ø', 'o')
                .replace("æ", "ae")
                .replace("Æ", "ae")
                .replace("ß", "ss")
            val nfkd = Normalizer.normalize(pre, Normalizer.Form.NFD)
            return nfkd.replace("\\p{M}".toRegex(), "").lowercase()
        }
    }

    @Volatile
    private var database: SQLiteDatabase? = null

    init {
        ensureDatabaseCopied()
    }

    @Synchronized
    private fun ensureDatabaseCopied() {
        val file = dbFile
        if (!file.exists() || file.length() < MIN_VALID_DB_SIZE) {
            file.parentFile?.mkdirs()
            assetManager.open(ASSET_PATH).use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(65536)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
        }
    }

    private fun getDb(): SQLiteDatabase {
        val existing = database
        if (existing != null && existing.isOpen) return existing
        return synchronized(this) {
            val doubleCheck = database
            if (doubleCheck != null && doubleCheck.isOpen) return doubleCheck
            ensureDatabaseCopied()
            val opened = SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
            database = opened
            opened
        }
    }

    fun getCityById(geonameId: Long): City? {
        val db = getDb()
        val cursor = db.rawQuery(
            """
            SELECT geoname_id, city_name, country_code, country_name, admin1_name,
                   latitude, longitude, zone_id, population, map_x, map_y, identity_color_hex
            FROM cities
            WHERE geoname_id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(geonameId.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToCity(it) else null
        }
    }

    fun getCitiesByIds(ids: Set<Long>): List<City> {
        if (ids.isEmpty()) return emptyList()
        val db = getDb()
        val placeholders = ids.joinToString(",") { "?" }
        val args = ids.map { it.toString() }.toTypedArray()
        val cursor = db.rawQuery(
            """
            SELECT geoname_id, city_name, country_code, country_name, admin1_name,
                   latitude, longitude, zone_id, population, map_x, map_y, identity_color_hex
            FROM cities
            WHERE geoname_id IN ($placeholders)
            """.trimIndent(),
            args
        )
        val result = mutableListOf<City>()
        cursor.use {
            while (it.moveToNext()) {
                result.add(cursorToCity(it))
            }
        }
        return result
    }

    fun searchCities(query: String, limit: Int = 60): List<City> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val norm = normalizeText(trimmed)
        val exact = norm
        val prefix = "$norm%"
        val exactAlt = "%,$norm,%"
        val prefixAlt = "%,$norm%"
        val upperCode = norm.uppercase()

        val db = getDb()
        val cursor = db.rawQuery(
            """
            SELECT geoname_id, city_name, country_code, country_name, admin1_name,
                   latitude, longitude, zone_id, population, map_x, map_y, identity_color_hex
            FROM cities
            WHERE ascii_name LIKE ?
               OR (',' || search_aliases || ',') LIKE ?
               OR country_name LIKE ?
               OR country_code = ?
               OR admin1_name LIKE ?
            ORDER BY
                CASE 
                    WHEN ascii_name = ? THEN 1
                    WHEN (',' || search_aliases || ',') LIKE ? THEN 2
                    WHEN ascii_name LIKE ? THEN 3
                    WHEN (',' || search_aliases || ',') LIKE ? THEN 4
                    WHEN country_name LIKE ? OR admin1_name LIKE ? THEN 5
                    ELSE 6
                END,
                population DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(
                prefix, prefixAlt, prefix, upperCode, prefix,
                exact, exactAlt, prefix, prefixAlt, prefix, prefix,
                limit.toString()
            )
        )

        val result = mutableListOf<City>()
        cursor.use {
            while (it.moveToNext()) {
                result.add(cursorToCity(it))
            }
        }
        return result
    }

    fun getTotalCityCount(): Long {
        val db = getDb()
        val cursor = db.rawQuery("SELECT COUNT(*) FROM cities", null)
        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else 0L
        }
    }

    private fun cursorToCity(cursor: Cursor): City {
        val gid = cursor.getLong(0)
        val cityName = cursor.getString(1)
        val countryCode = cursor.getString(2)
        val countryName = cursor.getString(3)
        val admin1Name = cursor.getString(4) ?: ""
        val latitude = cursor.getDouble(5)
        val longitude = cursor.getDouble(6)
        val zoneId = cursor.getString(7)
        val population = cursor.getLong(8)
        val mapX = cursor.getFloat(9)
        val mapY = cursor.getFloat(10)
        val identityColorHex = cursor.getString(11)

        return City(
            geonameId = gid,
            cityName = cityName,
            countryName = countryName,
            countryCode = countryCode,
            admin1Name = admin1Name,
            latitude = latitude,
            longitude = longitude,
            zoneIdString = zoneId,
            mapX = mapX,
            mapY = mapY,
            identityColorHex = identityColorHex,
            isSaved = false,
            population = population
        )
    }
}
