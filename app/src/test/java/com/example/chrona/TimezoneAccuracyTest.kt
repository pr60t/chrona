package com.example.chrona

import com.example.chrona.core.model.City
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class TimezoneAccuracyTest {

    @Test
    fun testPerpetualIanaDstTransitions() {
        // Verify London: GMT (UTC+0) in January, BST (UTC+1) in July
        val londonZone = ZoneId.of("Europe/London")
        val winterLondon = LocalDateTime.of(2026, 1, 15, 12, 0).atZone(ZoneOffset.UTC).toInstant()
        val summerLondon = LocalDateTime.of(2026, 7, 15, 12, 0).atZone(ZoneOffset.UTC).toInstant()

        assertEquals(ZoneOffset.ofHours(0), londonZone.rules.getOffset(winterLondon))
        assertEquals(ZoneOffset.ofHours(1), londonZone.rules.getOffset(summerLondon))

        // Verify New York: EST (UTC-5) in January, EDT (UTC-4) in July
        val nyZone = ZoneId.of("America/New_York")
        val winterNy = LocalDateTime.of(2026, 1, 15, 12, 0).atZone(ZoneOffset.UTC).toInstant()
        val summerNy = LocalDateTime.of(2026, 7, 15, 12, 0).atZone(ZoneOffset.UTC).toInstant()

        assertEquals(ZoneOffset.ofHours(-5), nyZone.rules.getOffset(winterNy))
        assertEquals(ZoneOffset.ofHours(-4), nyZone.rules.getOffset(summerNy))

        // Verify Berlin: CET (UTC+1) in January, CEST (UTC+2) in July
        val berlinZone = ZoneId.of("Europe/Berlin")
        assertEquals(ZoneOffset.ofHours(1), berlinZone.rules.getOffset(winterLondon))
        assertEquals(ZoneOffset.ofHours(2), berlinZone.rules.getOffset(summerLondon))

        // Verify Sydney (Southern Hemisphere): AEDT (UTC+11) in January, AEST (UTC+10) in July
        val sydneyZone = ZoneId.of("Australia/Sydney")
        assertEquals(ZoneOffset.ofHours(11), sydneyZone.rules.getOffset(winterLondon))
        assertEquals(ZoneOffset.ofHours(10), sydneyZone.rules.getOffset(summerLondon))

        // Verify Kathmandu: UTC+5:45 throughout, non-hourly offset, no DST
        val kathmanduZone = ZoneId.of("Asia/Kathmandu")
        val expectedKmOffset = ZoneOffset.ofHoursMinutes(5, 45)
        assertEquals(expectedKmOffset, kathmanduZone.rules.getOffset(winterLondon))
        assertEquals(expectedKmOffset, kathmanduZone.rules.getOffset(summerLondon))

        // Verify Tokyo: UTC+9 throughout, no DST
        val tokyoZone = ZoneId.of("Asia/Tokyo")
        assertEquals(ZoneOffset.ofHours(9), tokyoZone.rules.getOffset(winterLondon))
        assertEquals(ZoneOffset.ofHours(9), tokyoZone.rules.getOffset(summerLondon))
    }

    @Test
    fun testSingleInstantSynchronization() {
        val commonInstant = LocalDateTime.of(2026, 11, 20, 14, 30, 0)
            .atZone(ZoneOffset.UTC).toInstant()

        val utc = createTestCity("UTC", "UTC", 51.4778, 0.0)
        val london = createTestCity("London", "Europe/London", 51.5085, -0.1257)
        val ny = createTestCity("New York", "America/New_York", 40.7128, -74.0060)
        val tokyo = createTestCity("Tokyo", "Asia/Tokyo", 35.6895, 139.6917)
        val kathmandu = createTestCity("Kathmandu", "Asia/Kathmandu", 27.7017, 85.3206)

        // UTC is 14:30
        assertEquals("14:30", utc.formattedTime(is24Hour = true, now = commonInstant))
        // London in November is UTC+0 -> 14:30
        assertEquals("14:30", london.formattedTime(is24Hour = true, now = commonInstant))
        // New York in November is UTC-5 -> 09:30
        assertEquals("09:30", ny.formattedTime(is24Hour = true, now = commonInstant))
        // Tokyo is UTC+9 -> 23:30
        assertEquals("23:30", tokyo.formattedTime(is24Hour = true, now = commonInstant))
        // Kathmandu is UTC+5:45 -> 20:15
        assertEquals("20:15", kathmandu.formattedTime(is24Hour = true, now = commonInstant))
    }

    @Test
    fun testLocalDateAndWeekdayAcrossDateLine() {
        // Reference instant: Friday, 2026-11-20 at 22:00 UTC
        val fridayLateUtc = LocalDateTime.of(2026, 11, 20, 22, 0)
            .atZone(ZoneOffset.UTC).toInstant()

        // Auckland is UTC+13 in November: 22:00 + 13h = Saturday 11:00 AM on Nov 21
        val auckland = createTestCity("Auckland", "Pacific/Auckland", -36.8485, 174.7635)
        assertEquals(DayOfWeek.SATURDAY, auckland.localDate(fridayLateUtc).dayOfWeek)
        assertEquals("Sat", auckland.formattedWeekday(fridayLateUtc))

        // Honolulu is UTC-10: 22:00 - 10h = Friday 12:00 PM on Nov 20
        val honolulu = createTestCity("Honolulu", "Pacific/Honolulu", 21.3069, -157.8583)
        assertEquals(DayOfWeek.FRIDAY, honolulu.localDate(fridayLateUtc).dayOfWeek)
        assertEquals("Fri", honolulu.formattedWeekday(fridayLateUtc))
    }

    private fun createTestCity(name: String, zoneId: String, lat: Double, lon: Double): City {
        return City(
            geonameId = 100L,
            cityName = name,
            countryName = "Test",
            countryCode = "TT",
            admin1Name = "TestAdmin",
            latitude = lat,
            longitude = lon,
            zoneIdString = zoneId,
            mapX = 0f,
            mapY = 0f,
            identityColorHex = "#B8C7F2"
        )
    }
}
