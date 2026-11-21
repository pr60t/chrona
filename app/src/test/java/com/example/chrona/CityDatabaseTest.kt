package com.example.chrona

import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.database.CityCatalogueDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class CityDatabaseTest {

    @Test
    fun testTextNormalizationForAccentsAndDiacritics() {
        assertEquals("munchen", CityCatalogueDatabase.normalizeText("München"))
        assertEquals("sao paulo", CityCatalogueDatabase.normalizeText("São Paulo"))
        assertEquals("bogota", CityCatalogueDatabase.normalizeText("Bogotá"))
        assertEquals("reykjavik", CityCatalogueDatabase.normalizeText("Reykjavík"))
        assertEquals("koln", CityCatalogueDatabase.normalizeText("Köln"))
        assertEquals("lodz", CityCatalogueDatabase.normalizeText("Łódź"))
        assertEquals("kobenhavn", CityCatalogueDatabase.normalizeText("København"))
        assertEquals("dusseldorf", CityCatalogueDatabase.normalizeText("Düsseldorf"))
        assertEquals("zurich", CityCatalogueDatabase.normalizeText("Zürich"))
        assertEquals("tokyo", CityCatalogueDatabase.normalizeText("  TOKYO  "))
    }

    @Test
    fun testExactDefaultWorldClockEntriesAndOrdering() {
        val defaultIds = CityRepository.defaultCityIds
        assertEquals("Exactly four default cities must be configured", 4, defaultIds.size)

        val expected = listOf(
            Triple("UTC", "Etc/UTC", 1L),
            Triple("Tokyo", "Asia/Tokyo", 1850147L),
            Triple("Paris", "Europe/Paris", 2988507L),
            Triple("Munich", "Europe/Berlin", 2867714L)
        )

        for (i in expected.indices) {
            val (expectedName, expectedTz, expectedId) = expected[i]
            val city = CityRepository.getCityById(defaultIds[i])
            assertNotNull("City at index $i must exist", city)
            assertEquals("City name at index $i must match", expectedName, city!!.cityName)
            assertEquals("City geonameId at index $i must match", expectedId, city.geonameId)
            assertEquals("IANA ZoneId at index $i must match", ZoneId.of(expectedTz), city.zoneId)
        }
    }

    @Test
    fun testUtcSpecificAccuracy() {
        val utc = CityRepository.getCityById(1L)
        assertNotNull(utc)
        assertTrue(utc!!.isUtc)
        assertEquals(ZoneId.of("Etc/UTC"), utc.zoneId)
        assertNull("UTC has no physical coordinates so isDaytime must be null", utc.isDaytime())
    }

    @Test
    fun testDeleteAndRestorePreservesOrdering() = runBlocking {
        // Initial defaults: [UTC, Tokyo, Paris, Munich]
        val initialList = CityRepository.savedCities.value
        assertTrue(initialList.size >= 4)
        val parisId = 2988507L
        val originalIndex = initialList.indexOfFirst { it.geonameId == parisId }
        assertTrue("Paris should be in the initial saved list", originalIndex >= 0)

        // Delete Paris
        val deletedIndex = CityRepository.deleteSavedCity(parisId)
        assertEquals(originalIndex, deletedIndex)
        val afterDelete = CityRepository.savedCities.value
        assertFalse("Paris should no longer be in saved list", afterDelete.any { it.geonameId == parisId })

        // Restore Paris to original position
        CityRepository.restoreSavedCity(parisId, deletedIndex)
        val afterRestore = CityRepository.savedCities.value
        val restoredIndex = afterRestore.indexOfFirst { it.geonameId == parisId }
        assertEquals("Paris must be restored at its exact original position", originalIndex, restoredIndex)

        // Deleting from saved cities does NOT remove it from catalogue search
        val searchResults = CityRepository.searchCities("Paris")
        assertTrue("Paris must remain in master searchable catalogue", searchResults.any { it.cityName == "Paris" })
    }

    @Test
    fun testEmptySearchQueryReturnsEmptyList() = runBlocking {
        val emptyResults = CityRepository.searchCities("")
        assertTrue("Empty query should return empty list", emptyResults.isEmpty())

        val whitespaceResults = CityRepository.searchCities("   ")
        assertTrue("Whitespace query should return empty list", whitespaceResults.isEmpty())
    }

    @Test
    fun testFallbackSearchMatchesCityAndCountry() = runBlocking {
        val tokyoResults = CityRepository.searchCities("Tokyo")
        assertTrue("Tokyo should be found in search", tokyoResults.any { it.cityName == "Tokyo" })

        val ukResults = CityRepository.searchCities("United Kingdom")
        assertTrue("London should be found when querying country", ukResults.any { it.cityName == "London" })
    }
}
