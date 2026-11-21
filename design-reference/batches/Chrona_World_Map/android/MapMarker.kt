package com.chrona.app.ui.worldclock.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.hypot

data class CityMapMarker(
    val cityId: String,
    val cityName: String,
    val formattedTime: String,
    val utcOffsetLabel: String,
    val mapX: Float,
    val mapY: Float,
    val identityColor: Color,
    val isSelected: Boolean = false
)

/**
 * Separates visually overlapping markers (e.g., London/Paris, Warsaw/Berlin)
 * at render time while preserving immutable geographic coordinates.
 */
fun resolveMarkerDisplayOffsets(
    markers: List<CityMapMarker>,
    basePositions: List<Offset>,
    minSeparationPx: Float
): List<Offset> {
    val adjusted = basePositions.toMutableList()
    for (i in adjusted.indices) {
        for (j in i + 1 until adjusted.size) {
            val dx = adjusted[j].x - adjusted[i].x
            val dy = adjusted[j].y - adjusted[i].y
            val dist = hypot(dx, dy)
            if (dist < minSeparationPx) {
                val push = (minSeparationPx - dist) * 0.5f
                val ux = if (dist > 0.001f) dx / dist else 1f
                val uy = if (dist > 0.001f) dy / dist else 0f
                when {
                    markers[i].isSelected && !markers[j].isSelected -> {
                        adjusted[j] = Offset(adjusted[j].x + ux * push * 2f, adjusted[j].y + uy * push * 2f)
                    }
                    markers[j].isSelected && !markers[i].isSelected -> {
                        adjusted[i] = Offset(adjusted[i].x - ux * push * 2f, adjusted[i].y - uy * push * 2f)
                    }
                    else -> {
                        adjusted[i] = Offset(adjusted[i].x - ux * push, adjusted[i].y - uy * push)
                        adjusted[j] = Offset(adjusted[j].x + ux * push, adjusted[j].y + uy * push)
                    }
                }
            }
        }
    }
    return adjusted
}
