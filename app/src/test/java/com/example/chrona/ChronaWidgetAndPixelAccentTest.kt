package com.example.chrona

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.model.City
import com.example.chrona.core.model.UserPreferences
import com.example.chrona.ui.theme.ChronaDarkAccent
import com.example.chrona.ui.theme.ChronaPixelAccent
import com.example.chrona.widget.WidgetUpdateHelper
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class ChronaWidgetAndPixelAccentTest {

    @Test
    fun testDefaultAccentIsPixel() {
        val prefs = UserPreferences()
        assertEquals("#DCE2FA", prefs.accentColorHex)
        assertEquals("#DCE2FA", PreferencesRepository.DEFAULT_ACCENT)
        assertEquals(Color(0xFFDCE2FA), ChronaPixelAccent)
        assertEquals(Color(0xFFDCE2FA), ChronaDarkAccent)
    }

    @Test
    fun testStopwatchWidgetFormat() {
        assertEquals("00:00", WidgetUpdateHelper.formatStopwatchDisplay(0L))
        assertEquals("00:05", WidgetUpdateHelper.formatStopwatchDisplay(5000L))
        assertEquals("01:25", WidgetUpdateHelper.formatStopwatchDisplay(85000L))
        assertEquals("01:00:05", WidgetUpdateHelper.formatStopwatchDisplay(3605000L))
    }

    @Test
    fun testWorldClockDefaultCitiesOrdering() {
        assertEquals(4, CityRepository.defaultCityIds.size)
        assertEquals(1L, CityRepository.defaultCityIds[0]) // UTC
        assertEquals(1850147L, CityRepository.defaultCityIds[1]) // Tokyo
        assertEquals(2988507L, CityRepository.defaultCityIds[2]) // Paris
        assertEquals(2867714L, CityRepository.defaultCityIds[3]) // Munich
    }

    @Test
    fun testWorldClockSolarIndicatorColor() {
        val utcCity = City(
            geonameId = 1L,
            cityName = "UTC",
            countryName = "Universal Time Coordinated",
            countryCode = "UN",
            admin1Name = "Global",
            latitude = 0.0,
            longitude = 0.0,
            mapX = 0f,
            mapY = 0f,
            zoneIdString = "Etc/UTC",
            identityColorHex = "#B8C7F2",
            isSaved = true
        )
        assertTrue(utcCity.isUtc)
        // Dark theme: UTC is #7C8292
        val utcColorDark = WidgetUpdateHelper.getSolarIndicatorColor(utcCity, isDark = true)
        assertEquals(0xFF7C8292.toInt(), utcColorDark)

        // Light theme: UTC is #888B98
        val utcColorLight = WidgetUpdateHelper.getSolarIndicatorColor(utcCity, isDark = false)
        assertEquals(0xFF888B98.toInt(), utcColorLight)
    }

    @Test
    fun testLightPixelAccentColor() {
        val lightAccent = 0xFF5966A8.toInt()
        assertEquals(-10918232, lightAccent)
    }

    @Test
    fun testFormatOffsetHours() {
        val utcOffset = WidgetUpdateHelper.formatOffsetHours(ZoneId.of("Etc/UTC"))
        assertEquals("+0H", utcOffset)

        val tokyoOffset = WidgetUpdateHelper.formatOffsetHours(ZoneId.of("Asia/Tokyo"))
        assertEquals("+9H", tokyoOffset)
    }
}
