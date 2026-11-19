package com.example.chrona.core.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.min

/**
 * Exact Natural Earth (Savric et al., 2011) forward projection and viewport normalization
 * synchronized with world_map_projection.json and world_map_vector.xml (1200x600).
 */
object WorldMapProjection {
    const val VIEWPORT_WIDTH = 1200f
    const val VIEWPORT_HEIGHT = 600f

    private const val R_WGS84 = 6378137.0
    private const val MIN_X = -17446561.5882
    private const val MAX_X = 17446561.5882
    private const val MIN_Y = -6247632.5527
    private const val MAX_Y = 8816775.4882

    private const val CONTENT_LEFT = 24.0
    private const val CONTENT_TOP = 51.3235
    private const val UNIFORM_SCALE = 3.301510140485457e-05

    // Natural Earth polynomial coefficients
    private const val A0 = 0.870700
    private const val A1 = -0.131979
    private const val A2 = -0.013791
    private const val A3 = 0.003971
    private const val A4 = -0.001529

    private const val B0 = 1.007226
    private const val B1 = 0.015085
    private const val B2 = -0.044475
    private const val B3 = 0.028874
    private const val B4 = -0.005916

    fun latLonToNormalized(latitudeDeg: Double, longitudeDeg: Double): Offset {
        val phi = latitudeDeg.coerceIn(-56.0, 84.0) * PI / 180.0
        val lam = longitudeDeg.coerceIn(-179.999, 179.999) * PI / 180.0
        val phi2 = phi * phi
        val phi4 = phi2 * phi2
        val phi6 = phi4 * phi2

        val xMeters = R_WGS84 * lam * (A0 + A1 * phi2 + A2 * phi4 + phi6 * (A3 + A4 * phi2))
        val yMeters = R_WGS84 * phi * (B0 + B1 * phi2 + phi4 * (B2 + B3 * phi2 + B4 * phi4))

        val svgX = CONTENT_LEFT + (xMeters - MIN_X) * UNIFORM_SCALE
        val svgY = CONTENT_TOP + (MAX_Y - yMeters) * UNIFORM_SCALE

        return Offset(
            x = (svgX / VIEWPORT_WIDTH).toFloat(),
            y = (svgY / VIEWPORT_HEIGHT).toFloat()
        )
    }

    fun computeRenderedMapRect(containerSize: Size): Rect {
        if (containerSize.width <= 0f || containerSize.height <= 0f) return Rect.Zero
        val scale = min(
            containerSize.width / VIEWPORT_WIDTH,
            containerSize.height / VIEWPORT_HEIGHT
        )
        val renderedW = VIEWPORT_WIDTH * scale
        val renderedH = VIEWPORT_HEIGHT * scale
        val left = (containerSize.width - renderedW) / 2f
        val top = (containerSize.height - renderedH) / 2f
        return Rect(left, top, left + renderedW, top + renderedH)
    }

    fun normalizedToScreen(normalized: Offset, renderedMapRect: Rect): Offset {
        return Offset(
            x = renderedMapRect.left + normalized.x * renderedMapRect.width,
            y = renderedMapRect.top + normalized.y * renderedMapRect.height
        )
    }
}
