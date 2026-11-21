package com.example.chrona

import com.example.chrona.core.model.City
import com.example.chrona.core.timezone.SolarCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class SolarCalculatorTest {

    @Test
    fun testPolarDayMidnightSunInTromso() {
        // Tromsø, Norway (69.6492° N, 18.9553° E)
        // Summer solstice: June 21 at midnight UTC (01:00 or 02:00 local time).
        // The sun never sets (apparent elevation > -0.833°).
        val summerSolsticeMidnightUtc = LocalDateTime.of(2026, 6, 21, 23, 0)
            .atZone(ZoneOffset.UTC).toInstant()

        val elevation = SolarCalculator.calculateSolarElevation(
            instant = summerSolsticeMidnightUtc,
            latitudeDeg = 69.6492,
            longitudeDeg = 18.9553
        )

        assertTrue(
            "Polar day midnight sun elevation should be above threshold (-0.833°), got: $elevation",
            elevation > -0.833
        )

        val isDay = SolarCalculator.isDaytime(
            instant = summerSolsticeMidnightUtc,
            latitudeDeg = 69.6492,
            longitudeDeg = 18.9553
        )
        assertTrue("Polar day in Tromsø must evaluate to daytime", isDay)
    }

    @Test
    fun testPolarNightInTromso() {
        // Tromsø, Norway (69.6492° N, 18.9553° E)
        // Winter solstice: Dec 21 at solar noon (11:00 UTC).
        // The sun never rises above the horizon (elevation < -0.833°).
        val winterSolsticeNoonUtc = LocalDateTime.of(2026, 12, 21, 11, 0)
            .atZone(ZoneOffset.UTC).toInstant()

        val elevation = SolarCalculator.calculateSolarElevation(
            instant = winterSolsticeNoonUtc,
            latitudeDeg = 69.6492,
            longitudeDeg = 18.9553
        )

        assertTrue(
            "Polar night noon elevation should be below threshold (-0.833°), got: $elevation",
            elevation < -0.833
        )

        val isDay = SolarCalculator.isDaytime(
            instant = winterSolsticeNoonUtc,
            latitudeDeg = 69.6492,
            longitudeDeg = 18.9553
        )
        assertFalse("Polar night in Tromsø must evaluate to night", isDay)
    }

    @Test
    fun testEquatorSolarNoonAndMidnight() {
        // Pontianak, Indonesia (0.0° N, 109.3333° E)
        // Spring equinox: March 20, 2026
        // Longitude 109.3333° corresponds to UTC+7:17 solar time.
        // Solar noon is roughly 04:43 UTC.
        val solarNoon = LocalDateTime.of(2026, 3, 20, 4, 43)
            .atZone(ZoneOffset.UTC).toInstant()

        val elevationNoon = SolarCalculator.calculateSolarElevation(
            instant = solarNoon,
            latitudeDeg = 0.0,
            longitudeDeg = 109.3333
        )
        assertTrue("Equator noon elevation should be near 90°, got: $elevationNoon", elevationNoon > 80.0)
        assertTrue("Equator noon should be daytime", SolarCalculator.isDaytime(solarNoon, 0.0, 109.3333))

        // Solar midnight is roughly 16:43 UTC.
        val solarMidnight = LocalDateTime.of(2026, 3, 20, 16, 43)
            .atZone(ZoneOffset.UTC).toInstant()

        val elevationMidnight = SolarCalculator.calculateSolarElevation(
            instant = solarMidnight,
            latitudeDeg = 0.0,
            longitudeDeg = 109.3333
        )
        assertTrue("Equator midnight elevation should be near -90°, got: $elevationMidnight", elevationMidnight < -80.0)
        assertFalse("Equator midnight should be night", SolarCalculator.isDaytime(solarMidnight, 0.0, 109.3333))
    }

    @Test
    fun testCityModelIntegration() {
        val tokyo = City(
            geonameId = 1850147L,
            cityName = "Tokyo",
            countryName = "Japan",
            countryCode = "JP",
            admin1Name = "Tokyo",
            latitude = 35.6895,
            longitude = 139.69171,
            zoneIdString = "Asia/Tokyo",
            mapX = 0.88f,
            mapY = 0.35f,
            identityColorHex = "#E7A19A"
        )

        // Midday in Tokyo: 2026-05-15 03:00 UTC (12:00 JST)
        val tokyoMidday = LocalDateTime.of(2026, 5, 15, 3, 0).atZone(ZoneOffset.UTC).toInstant()
        assertTrue("Midday in Tokyo should be daytime", tokyo.isDaytime(tokyoMidday) == true)

        // Midnight in Tokyo: 2026-05-15 15:00 UTC (00:00 JST)
        val tokyoMidnight = LocalDateTime.of(2026, 5, 15, 15, 0).atZone(ZoneOffset.UTC).toInstant()
        assertTrue("Midnight in Tokyo should be night", tokyo.isDaytime(tokyoMidnight) == false)
    }
}
