package com.example.chrona.core.timezone

import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.*

/**
 * Astronomical Solar Position Calculator based on standard NOAA solar geometry algorithms.
 * Calculates true solar elevation and determines Day/Night status without network or weather APIs.
 *
 * Day/Night threshold:
 * -0.833° of solar elevation (accounts for 34 arcminutes of atmospheric refraction and 16 arcminutes solar radius).
 * - elevation >= -0.833° : Day
 * - elevation < -0.833°  : Night
 */
object SolarCalculator {

    const val SUNRISE_SUNSET_THRESHOLD_DEG = -0.833

    /**
     * Calculates the apparent solar elevation in degrees above/below the astronomical horizon.
     *
     * @param instant The exact UTC Instant.
     * @param latitudeDeg Latitude in decimal degrees (-90.0 to +90.0).
     * @param longitudeDeg Longitude in decimal degrees (-180.0 to +180.0).
     * @return Apparent solar elevation in degrees.
     */
    fun calculateSolarElevation(
        instant: Instant,
        latitudeDeg: Double,
        longitudeDeg: Double
    ): Double {
        val epochMs = instant.toEpochMilli().toDouble()
        // Julian Day from Unix epoch
        val jd = 2440587.5 + (epochMs / 86400000.0)
        // Julian Century from J2000.0
        val jc = (jd - 2451545.0) / 36525.0

        // Geometric Mean Longitude of Sun (degrees)
        val l0 = (280.46646 + jc * (36000.76983 + jc * 0.0003032)) % 360.0
        // Geometric Mean Anomaly of Sun (degrees)
        val m = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)
        // Eccentricity of Earth's orbit
        val e = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)

        val mRad = Math.toRadians(m)
        // Sun's Equation of Center
        val c = (sin(mRad) * (1.914602 - jc * (0.004817 + 0.000014 * jc)) +
                sin(2.0 * mRad) * (0.019993 - 0.000101 * jc) +
                sin(3.0 * mRad) * 0.000289)
        val trueLong = l0 + c

        // Apparent Longitude of Sun
        val omega = 125.04 - 1934.136 * jc
        val lambdaDeg = trueLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))

        // Obliquity of Ecliptic
        val eps0 = 23.0 + (26.0 + (21.448 - jc * (46.8150 + jc * (0.00059 - jc * 0.001813))) / 60.0) / 60.0
        val eps = eps0 + 0.00256 * cos(Math.toRadians(omega))

        // Sun Declination
        val sinDelta = sin(Math.toRadians(eps)) * sin(Math.toRadians(lambdaDeg))
        val deltaRad = asin(sinDelta.coerceIn(-1.0, 1.0))

        // Equation of Time (minutes)
        val y = tan(Math.toRadians(eps) / 2.0).pow(2)
        val l0Rad = Math.toRadians(l0)
        val eqTimeMinutes = 4.0 * Math.toDegrees(
            y * sin(2.0 * l0Rad) - 2.0 * e * sin(mRad) +
            4.0 * e * y * sin(mRad) * cos(2.0 * l0Rad) -
            0.5 * (y * y) * sin(4.0 * l0Rad) -
            1.25 * (e * e) * sin(2.0 * mRad)
        )

        // UTC seconds of the day
        val zdtUtc = instant.atOffset(ZoneOffset.UTC)
        val utcSeconds = zdtUtc.hour * 3600.0 + zdtUtc.minute * 60.0 + zdtUtc.second + (zdtUtc.nano / 1e9)
        // Time offset from UTC in seconds
        val timeOffsetSec = eqTimeMinutes * 60.0 + longitudeDeg * 240.0
        var trueSolarTimeSec = (utcSeconds + timeOffsetSec) % 86400.0
        if (trueSolarTimeSec < 0) trueSolarTimeSec += 86400.0

        // Solar Hour Angle (-180° at solar midnight, 0° at solar noon, +180° at solar midnight)
        val hourAngleDeg = (trueSolarTimeSec / 240.0) - 180.0
        val hRad = Math.toRadians(hourAngleDeg)
        val latRad = Math.toRadians(latitudeDeg)

        // Solar elevation
        val sinAlpha = sin(latRad) * sin(deltaRad) + cos(latRad) * cos(deltaRad) * cos(hRad)
        val alphaDeg = Math.toDegrees(asin(sinAlpha.coerceIn(-1.0, 1.0)))

        return alphaDeg
    }

    /**
     * Determines whether the Sun is above the horizon (-0.833° threshold) for the given coordinates and instant.
     * Correctly handles polar day (24h Sun), polar night (24h darkness), equator, and high-latitude transitions.
     */
    fun isDaytime(
        instant: Instant,
        latitudeDeg: Double,
        longitudeDeg: Double
    ): Boolean {
        return calculateSolarElevation(instant, latitudeDeg, longitudeDeg) >= SUNRISE_SUNSET_THRESHOLD_DEG
    }
}
