package com.example.chrona

import androidx.compose.ui.geometry.Size
import com.example.chrona.core.map.WorldMapProjection
import org.junit.Assert.*
import org.junit.Test

class WorldMapProjectionTest {

    @Test
    fun testGreenwichProjectionCoordinates() {
        // Greenwich: 51.47785 N, -0.01176 W
        // Expected mapX ~ 0.49997, mapY ~ 0.25339
        val normalized = WorldMapProjection.latLonToNormalized(51.47785, -0.01176)

        assertEquals(0.49997f, normalized.x, 0.005f)
        assertEquals(0.25339f, normalized.y, 0.005f)
    }

    @Test
    fun testTokyoProjectionCoordinates() {
        // Tokyo: 35.6895 N, 139.69171 E
        // Expected mapX ~ 0.8497, mapY ~ 0.3496
        val normalized = WorldMapProjection.latLonToNormalized(35.6895, 139.69171)

        assertEquals(0.8497f, normalized.x, 0.005f)
        assertEquals(0.3496f, normalized.y, 0.005f)
    }

    @Test
    fun testKathmanduProjectionCoordinates() {
        // Kathmandu: 27.70169 N, 85.3206 E
        // Expected mapX ~ 0.7192, mapY ~ 0.3992
        val normalized = WorldMapProjection.latLonToNormalized(27.70169, 85.3206)

        assertEquals(0.7192f, normalized.x, 0.005f)
        assertEquals(0.3992f, normalized.y, 0.005f)
    }

    @Test
    fun testComputeRenderedMapRectPreservesAspectRatio() {
        // Viewport is 1200 x 600 (aspect ratio 2:1)
        val containerSize = Size(1000f, 800f)
        val rect = WorldMapProjection.computeRenderedMapRect(containerSize)

        // Width constrained: scale = 1000 / 1200 = 0.8333
        // renderedW = 1000, renderedH = 500
        assertEquals(0f, rect.left, 0.001f)
        assertEquals(1000f, rect.width, 0.001f)
        assertEquals(500f, rect.height, 0.001f)
        assertEquals(150f, rect.top, 0.001f) // (800 - 500) / 2
    }
}
