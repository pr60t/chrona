package com.example.chrona.core.data

import android.content.Context
import com.example.chrona.core.database.CityCatalogueDatabase
import com.example.chrona.core.database.SavedCityDao
import com.example.chrona.core.map.WorldMapProjection
import com.example.chrona.core.model.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CityRepository {

    // Initial default city IDs from the design spec (UTC, Tokyo, Paris, Munich)
    val defaultCityIds = listOf(1L, 1850147L, 2988507L, 2867714L)

    private val fallbackCities: List<City> = listOf(
        createCity(
            geonameId = 1L,
            cityName = "UTC",
            countryName = "Universal Time Coordinated",
            countryCode = "UN",
            admin1Name = "Global",
            latitude = 0.0,
            longitude = 0.0,
            zoneId = "Etc/UTC",
            identityColorHex = "#B8C7F2",
            isSaved = true
        ),
        createCity(
            geonameId = 756135L,
            cityName = "Warsaw",
            countryName = "Poland",
            countryCode = "PL",
            admin1Name = "Mazovia",
            latitude = 52.22977,
            longitude = 21.01178,
            zoneId = "Europe/Warsaw",
            identityColorHex = "#A7E8D8",
            isSaved = true,
            population = 1702139
        ),
        createCity(
            geonameId = 2643743L,
            cityName = "London",
            countryName = "United Kingdom",
            countryCode = "GB",
            admin1Name = "England",
            latitude = 51.50853,
            longitude = -0.12574,
            zoneId = "Europe/London",
            identityColorHex = "#B8C7F2",
            isSaved = true,
            population = 8961989
        ),
        createCity(
            geonameId = 4930956L,
            cityName = "Boston",
            countryName = "United States",
            countryCode = "US",
            admin1Name = "Massachusetts",
            latitude = 42.35843,
            longitude = -71.05977,
            zoneId = "America/New_York",
            identityColorHex = "#F4C2A1",
            isSaved = true,
            population = 675647
        ),
        createCity(
            geonameId = 2063523L,
            cityName = "Perth",
            countryName = "Australia",
            countryCode = "AU",
            admin1Name = "Western Australia",
            latitude = -31.95224,
            longitude = 115.8614,
            zoneId = "Australia/Perth",
            identityColorHex = "#D4B8F0",
            isSaved = true,
            population = 2384371
        ),
        createCity(
            geonameId = 1850147L,
            cityName = "Tokyo",
            countryName = "Japan",
            countryCode = "JP",
            admin1Name = "Tokyo",
            latitude = 35.6895,
            longitude = 139.69171,
            zoneId = "Asia/Tokyo",
            identityColorHex = "#E7A19A",
            isSaved = true,
            population = 9733276
        ),
        createCity(
            geonameId = 5128581L,
            cityName = "New York City",
            countryName = "United States",
            countryCode = "US",
            admin1Name = "New York",
            latitude = 40.71427,
            longitude = -74.00597,
            zoneId = "America/New_York",
            identityColorHex = "#EAB994",
            isSaved = false,
            population = 8804190
        ),
        createCity(
            geonameId = 2988507L,
            cityName = "Paris",
            countryName = "France",
            countryCode = "FR",
            admin1Name = "Ile-de-France",
            latitude = 48.85341,
            longitude = 2.3488,
            zoneId = "Europe/Paris",
            identityColorHex = "#C39BE2",
            isSaved = true,
            population = 2138551
        ),
        createCity(
            geonameId = 2867714L,
            cityName = "Munich",
            countryName = "Germany",
            countryCode = "DE",
            admin1Name = "Bavaria",
            latitude = 48.13743,
            longitude = 11.57549,
            zoneId = "Europe/Berlin",
            identityColorHex = "#A7E8D8",
            isSaved = true,
            population = 1505005
        ),
        createCity(
            geonameId = 2147714L,
            cityName = "Sydney",
            countryName = "Australia",
            countryCode = "AU",
            admin1Name = "New South Wales",
            latitude = -33.86785,
            longitude = 151.20732,
            zoneId = "Australia/Sydney",
            identityColorHex = "#78C7C7",
            isSaved = false,
            population = 5638830
        ),
        createCity(
            geonameId = 3369157L,
            cityName = "Cape Town",
            countryName = "South Africa",
            countryCode = "ZA",
            admin1Name = "Western Cape",
            latitude = -33.92584,
            longitude = 18.42322,
            zoneId = "Africa/Johannesburg",
            identityColorHex = "#A3C7DE",
            isSaved = false,
            population = 4772846
        ),
        createCity(
            geonameId = 3448439L,
            cityName = "São Paulo",
            countryName = "Brazil",
            countryCode = "BR",
            admin1Name = "Sao Paulo",
            latitude = -23.5475,
            longitude = -46.63611,
            zoneId = "America/Sao_Paulo",
            identityColorHex = "#8FD8B9",
            isSaved = false,
            population = 12400232
        ),
        createCity(
            geonameId = 5856195L,
            cityName = "Honolulu",
            countryName = "United States",
            countryCode = "US",
            admin1Name = "Hawaii",
            latitude = 21.30694,
            longitude = -157.85833,
            zoneId = "Pacific/Honolulu",
            identityColorHex = "#EAB994",
            isSaved = false,
            population = 350964
        ),
        createCity(
            geonameId = 360630L,
            cityName = "Cairo",
            countryName = "Egypt",
            countryCode = "EG",
            admin1Name = "Cairo",
            latitude = 30.06263,
            longitude = 31.24967,
            zoneId = "Africa/Cairo",
            identityColorHex = "#85A8E5",
            isSaved = false,
            population = 9606916
        ),
        createCity(
            geonameId = 292223L,
            cityName = "Dubai",
            countryName = "United Arab Emirates",
            countryCode = "AE",
            admin1Name = "Dubai",
            latitude = 25.0657,
            longitude = 55.17128,
            zoneId = "Asia/Dubai",
            identityColorHex = "#F2DF9B",
            isSaved = false,
            population = 3331420
        ),
        createCity(
            geonameId = 1880252L,
            cityName = "Singapore",
            countryName = "Singapore",
            countryCode = "SG",
            admin1Name = "Singapore",
            latitude = 1.35208,
            longitude = 103.81984,
            zoneId = "Asia/Singapore",
            identityColorHex = "#8FD8B9",
            isSaved = false,
            population = 5685807
        ),
        createCity(
            geonameId = 1819729L,
            cityName = "Hong Kong",
            countryName = "Hong Kong",
            countryCode = "HK",
            admin1Name = "Hong Kong",
            latitude = 22.27832,
            longitude = 114.17469,
            zoneId = "Asia/Hong_Kong",
            identityColorHex = "#E7A19A",
            isSaved = false,
            population = 7482500
        ),
        createCity(
            geonameId = 2950159L,
            cityName = "Berlin",
            countryName = "Germany",
            countryCode = "DE",
            admin1Name = "Berlin",
            latitude = 52.52437,
            longitude = 13.41053,
            zoneId = "Europe/Berlin",
            identityColorHex = "#B8C7F2",
            isSaved = false,
            population = 3644826
        ),
        createCity(
            geonameId = 5368361L,
            cityName = "Los Angeles",
            countryName = "United States",
            countryCode = "US",
            admin1Name = "California",
            latitude = 34.05223,
            longitude = -118.24368,
            zoneId = "America/Los_Angeles",
            identityColorHex = "#F4C2A1",
            isSaved = false,
            population = 3979576
        ),
        createCity(
            geonameId = 6167865L,
            cityName = "Toronto",
            countryName = "Canada",
            countryCode = "CA",
            admin1Name = "Ontario",
            latitude = 43.70011,
            longitude = -79.4163,
            zoneId = "America/Toronto",
            identityColorHex = "#A3C7DE",
            isSaved = false,
            population = 2731571
        ),
        createCity(
            geonameId = 1835848L,
            cityName = "Seoul",
            countryName = "South Korea",
            countryCode = "KR",
            admin1Name = "Seoul",
            latitude = 37.566,
            longitude = 126.9784,
            zoneId = "Asia/Seoul",
            identityColorHex = "#8FD8B9",
            isSaved = false,
            population = 10349312
        ),
        createCity(
            geonameId = 1609350L,
            cityName = "Bangkok",
            countryName = "Thailand",
            countryCode = "TH",
            admin1Name = "Bangkok",
            latitude = 13.75398,
            longitude = 100.50144,
            zoneId = "Asia/Bangkok",
            identityColorHex = "#E3EAA6",
            isSaved = false,
            population = 5104476
        ),
        createCity(
            geonameId = 1273294L,
            cityName = "Delhi",
            countryName = "India",
            countryCode = "IN",
            admin1Name = "Delhi",
            latitude = 28.65195,
            longitude = 77.23149,
            zoneId = "Asia/Kolkata",
            identityColorHex = "#F2DF9B",
            isSaved = false,
            population = 11034555
        ),
        createCity(
            geonameId = 2193733L,
            cityName = "Auckland",
            countryName = "New Zealand",
            countryCode = "NZ",
            admin1Name = "Auckland",
            latitude = -36.84853,
            longitude = 174.76349,
            zoneId = "Pacific/Auckland",
            identityColorHex = "#78C7C7",
            isSaved = false,
            population = 1470100
        ),
        createCity(
            geonameId = 3413829L,
            cityName = "Reykjavik",
            countryName = "Iceland",
            countryCode = "IS",
            admin1Name = "Capital Region",
            latitude = 64.13548,
            longitude = -21.89541,
            zoneId = "Atlantic/Reykjavik",
            identityColorHex = "#B8C7F2",
            isSaved = false,
            population = 118918
        ),
        createCity(
            geonameId = 2657896L,
            cityName = "Zurich",
            countryName = "Switzerland",
            countryCode = "CH",
            admin1Name = "Zurich",
            latitude = 47.36667,
            longitude = 8.55,
            zoneId = "Europe/Zurich",
            identityColorHex = "#A7E8D8",
            isSaved = false,
            population = 341730
        )
    )

    private val savedCityIds = MutableStateFlow<List<Long>>(defaultCityIds)
    private val _savedCities = MutableStateFlow<List<City>>(
        fallbackCities.filter { defaultCityIds.contains(it.geonameId) }
    )
    val savedCities: StateFlow<List<City>> = _savedCities.asStateFlow()

    @Volatile
    private var catalogueDb: CityCatalogueDatabase? = null
    private var savedCityDao: SavedCityDao? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        val appCtx = context.applicationContext
        appContext = appCtx
        if (savedCityDao == null) {
            catalogueDb = CityCatalogueDatabase.getInstance(appCtx)
            val dao = SavedCityDao(appCtx)
            savedCityDao = dao
            scope.launch {
                val oldSystemDefaults = setOf(756135L, 2643743L, 4930956L, 2063523L, 1283240L, 2950159L)
                val persisted = dao.getSavedCityIds().toList()
                if (persisted.isEmpty()) {
                    dao.replaceAll(defaultCityIds)
                    savedCityIds.value = defaultCityIds
                } else if (persisted.any { it in oldSystemDefaults }) {
                    // One-time migration: replace old system defaults with new defaults while keeping user-added cities
                    val userAdded = persisted.filter { it !in oldSystemDefaults && it !in defaultCityIds }
                    val migrated = (defaultCityIds + userAdded).distinct()
                    dao.replaceAll(migrated)
                    savedCityIds.value = migrated
                } else {
                    savedCityIds.value = persisted
                }
                updateSavedCities()
            }
        }
    }

    private fun updateSavedCitiesSync() {
        val currentIds = savedCityIds.value
        val db = catalogueDb
        val dbCities = db?.getCitiesByIds(currentIds.toSet()) ?: emptyList()
        val fallbackMap = fallbackCities.associateBy { it.geonameId }
        val dbMap = dbCities.associateBy { it.geonameId }

        val ordered = currentIds.mapNotNull { id ->
            (dbMap[id] ?: fallbackMap[id])?.copy(isSaved = true)
        }
        _savedCities.value = ordered

        appContext?.let { ctx ->
            try {
                com.example.chrona.widget.WidgetUpdateHelper.updateWorldClockAtlasWidgets(ctx)
                com.example.chrona.widget.WidgetUpdateHelper.updateWorldClockMinimalWidgets(ctx)
                com.example.chrona.widget.WidgetUpdateHelper.updateWorldClockFrostWidgets(ctx)
            } catch (_: Exception) {}
        }
    }

    private suspend fun updateSavedCities() = withContext(Dispatchers.IO) {
        updateSavedCitiesSync()
    }

    fun getAllCities(): List<City> = fallbackCities

    suspend fun searchCities(query: String, limit: Int = 60): List<City> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val db = catalogueDb
        val results = if (db != null) {
            db.searchCities(trimmed, limit)
        } else {
            fallbackSearch(trimmed)
        }

        val savedSet = savedCityIds.value.toSet()
        results.map { city ->
            if (savedSet.contains(city.geonameId)) city.copy(isSaved = true) else city
        }
    }

    private fun fallbackSearch(query: String): List<City> {
        val lower = query.lowercase()
        return fallbackCities.filter {
            it.cityName.lowercase().contains(lower) ||
            it.countryName.lowercase().contains(lower) ||
            it.admin1Name.lowercase().contains(lower) ||
            it.zoneIdString.lowercase().contains(lower)
        }
    }

    fun deleteSavedCity(cityId: Long): Int {
        val current = savedCityIds.value.toMutableList()
        val index = current.indexOf(cityId)
        if (index != -1) {
            current.removeAt(index)
            savedCityIds.value = current
            updateSavedCitiesSync()
            val dao = savedCityDao
            scope.launch {
                dao?.replaceAll(current)
            }
        }
        return index
    }

    fun restoreSavedCity(cityId: Long, originalIndex: Int) {
        val current = savedCityIds.value.toMutableList()
        if (!current.contains(cityId)) {
            val targetIndex = originalIndex.coerceIn(0, current.size)
            current.add(targetIndex, cityId)
            savedCityIds.value = current
            updateSavedCitiesSync()
            val dao = savedCityDao
            scope.launch {
                dao?.replaceAll(current)
            }
        }
    }

    fun toggleSaveCity(cityId: Long) {
        val current = savedCityIds.value.toMutableList()
        if (current.contains(cityId)) {
            current.remove(cityId)
        } else {
            current.add(cityId)
        }
        savedCityIds.value = current
        updateSavedCitiesSync()
        val dao = savedCityDao
        scope.launch {
            dao?.replaceAll(current)
        }
    }

    fun getCityById(cityId: Long): City? {
        val inSaved = _savedCities.value.find { it.geonameId == cityId }
        if (inSaved != null) return inSaved

        val fromDb = catalogueDb?.getCityById(cityId)
        if (fromDb != null) {
            val isSaved = savedCityIds.value.contains(cityId)
            return if (isSaved) fromDb.copy(isSaved = true) else fromDb
        }
        return fallbackCities.find { it.geonameId == cityId }
    }

    fun findCityByZoneId(zoneId: String): City? {
        val inSaved = _savedCities.value.find { it.zoneIdString == zoneId }
        if (inSaved != null) return inSaved

        return fallbackCities.find { it.zoneIdString == zoneId }
    }

    private fun createCity(
        geonameId: Long,
        cityName: String,
        countryName: String,
        countryCode: String,
        admin1Name: String,
        latitude: Double,
        longitude: Double,
        zoneId: String,
        identityColorHex: String,
        isSaved: Boolean = false,
        population: Long = 0
    ): City {
        val norm = WorldMapProjection.latLonToNormalized(latitude, longitude)
        return City(
            geonameId = geonameId,
            cityName = cityName,
            countryName = countryName,
            countryCode = countryCode,
            admin1Name = admin1Name,
            latitude = latitude,
            longitude = longitude,
            zoneIdString = zoneId,
            mapX = norm.x,
            mapY = norm.y,
            identityColorHex = identityColorHex,
            isSaved = isSaved,
            population = population
        )
    }
}
