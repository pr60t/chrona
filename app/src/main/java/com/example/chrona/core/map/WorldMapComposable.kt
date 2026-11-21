package com.example.chrona.core.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.chrona.R
import com.example.chrona.ui.theme.ChronaThemeTokens
import kotlin.math.roundToInt

@Composable
fun ChronaWorldMapHeader(
    markers: List<CityMapMarker>,
    collapseFraction: Float,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = ChronaThemeTokens.isDark
    val fraction = collapseFraction.coerceIn(0f, 1f)
    val containerHeight = lerp(196.dp, 56.dp, fraction)
    val mapAlpha = 1f - (fraction * 0.65f)
    val scaleFactor = 1f - (fraction * 0.35f)

    val pulseTransition = rememberInfiniteTransition(label = "selectedMarkerPulse")
    val pulseRadiusMultiplier by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    val extendedColors = ChronaThemeTokens.extended
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(containerHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(extendedColors.surfaceLowest)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
        val density = LocalDensity.current
        val containerSizePx = with(density) { Size(maxWidth.toPx(), maxHeight.toPx()) }
        val renderedRect = remember(containerSizePx) {
            WorldMapProjection.computeRenderedMapRect(containerSizePx, fillBounds = true)
        }

        // Theme-aware VectorDrawable background (Light vs Dark)
        val mapDrawableId = if (isDark) R.drawable.world_map_vector else R.drawable.world_map_vector_light
        Image(
            painter = painterResource(id = mapDrawableId),
            contentDescription = "World timezone map",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .alpha(mapAlpha)
        )

        val rawPositions = remember(markers, renderedRect) {
            markers.map { m ->
                WorldMapProjection.normalizedToScreen(Offset(m.mapX, m.mapY), renderedRect)
            }
        }
        val minSepPx = with(density) { (14.dp * scaleFactor).toPx() }
        val displayPositions = remember(markers, rawPositions, minSepPx) {
            resolveMarkerDisplayOffsets(markers, rawPositions, minSepPx)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            markers.forEachIndexed { index, marker ->
                val truePos = rawPositions[index]
                val drawPos = displayPositions[index]

                if ((drawPos - truePos).getDistance() > 2.dp.toPx()) {
                    drawLine(
                        color = marker.identityColor.copy(alpha = 0.45f),
                        start = truePos,
                        end = drawPos,
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                val coreRadius = (if (marker.isSelected) 5.dp else 3.75.dp).toPx() * scaleFactor
                val baseHaloRadius = (if (marker.isSelected) 13.dp else 8.5.dp).toPx() * scaleFactor
                val haloRadius = if (marker.isSelected) baseHaloRadius * pulseRadiusMultiplier else baseHaloRadius

                drawCircle(
                    color = marker.identityColor.copy(alpha = if (marker.isSelected) 0.32f else 0.20f),
                    radius = haloRadius,
                    center = drawPos
                )
                drawCircle(
                    color = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF),
                    radius = coreRadius + 1.25.dp.toPx() * scaleFactor,
                    center = drawPos
                )
                drawCircle(
                    color = marker.identityColor,
                    radius = coreRadius,
                    center = drawPos
                )
            }
        }

        markers.forEachIndexed { index, marker ->
            val drawPos = displayPositions[index]
            val hitSize = 36.dp
            val hitHalfPx = with(density) { (hitSize / 2).toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (drawPos.x - hitHalfPx).roundToInt(),
                            y = (drawPos.y - hitHalfPx).roundToInt()
                        )
                    }
                    .size(hitSize)
                    .semantics {
                        role = Role.Button
                        contentDescription = "${marker.cityName}, ${marker.formattedTime}, ${marker.utcOffsetLabel}"
                    }
                    .clickable { onMarkerClick(marker.cityId) }
            )

            if (marker.isSelected && fraction < 0.45f) {
                val pillOffsetY = with(density) { 28.dp.toPx() }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF1A202C) else Color(0xFFFFFFFF),
                    border = BorderStroke(1.dp, marker.identityColor),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (drawPos.x - with(density) { 48.dp.toPx() }).roundToInt(),
                                y = (drawPos.y - pillOffsetY).roundToInt()
                            )
                        }
                        .alpha(1f - (fraction / 0.45f))
                ) {
                    Text(
                        text = "${marker.cityName} · ${marker.formattedTime}",
                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
}
