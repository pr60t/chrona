package com.example.chrona.feature.alarms

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.model.Alarm
import com.example.chrona.core.timezone.TimezoneEngine
import com.example.chrona.ui.theme.ChronaDarkBg
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RingingAlarmScreen(
    alarm: Alarm,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = Instant.now()
        }
    }

    val targetZdt = now.atZone(alarm.targetZoneId)
    val localPreview = remember(now) {
        TimezoneEngine.previewLocalTime(alarm, referenceInstant = now)
    }

    val pulseTransition = rememberInfiniteTransition(label = "ringingPulse")
    val pulseRingRadius by pulseTransition.animateFloat(
        initialValue = 124f,
        targetValue = 162f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val accent = alarm.identityColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChronaDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Timezone / City pill and Alarm Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1A202C),
                    border = BorderStroke(1.2.dp, accent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Text(
                            text = if (alarm.isCrossTimezone) {
                                "${alarm.targetCityName.uppercase()} · ${alarm.targetZoneIdString.uppercase()}"
                            } else {
                                "LOCAL ALARM · DEVICE TIME"
                            },
                            color = Color(0xFFF1F5F9),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Label and timezone subtitle
                Text(
                    text = alarm.label,
                    color = Color(0xFFF1F5F9),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (alarm.isCrossTimezone) "DESTINATION ALARM TIME" else "SCHEDULED ALARM TIME",
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            // MIDDLE SECTION: Shared Centered Anchor for Pulsing Circles & Pixel Scalloped Clock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Concentric Ripple Rings (sharing exact center anchor)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Ambient Radial Gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.22f),
                                accent.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = size.width * 0.48f
                        ),
                        center = centerOffset,
                        radius = size.width * 0.48f
                    )

                    // Outer pulse ring
                    drawCircle(
                        color = accent.copy(alpha = 0.16f),
                        radius = pulseRingRadius * density,
                        center = centerOffset,
                        style = Stroke(width = 1.2f * density)
                    )

                    // Inner pulse ring
                    drawCircle(
                        color = accent.copy(alpha = pulseAlpha),
                        radius = (pulseRingRadius - 32f).coerceAtLeast(10f) * density,
                        center = centerOffset,
                        style = Stroke(width = 1.5f * density)
                    )
                }

                // The Pixel / Material UI 12-lobed Scalloped Clock centered precisely in the pulse
                PixelScallopedClock(
                    targetZonedDateTime = targetZdt,
                    accentColor = accent,
                    modifier = Modifier
                        .size(220.dp)
                        .align(Alignment.Center)
                )

                // Date positioned independently underneath the clock (not altering clock center)
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 110.dp + 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = targetZdt.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())),
                        color = Color(0xFF94A3B8),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (alarm.isCrossTimezone) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A202C),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2DF9B))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DEVICE LOCAL: ${localPreview.deviceTime} ${localPreview.deviceAmPm} (${localPreview.dayOffsetDescription})",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                )
                            }
                        }
                    }
                }
            }

            // BOTTOM SECTION: Large accessible Snooze + Slide to Dismiss
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Snooze Pill (Height 68dp, rx 34dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .clickable { onSnooze() },
                    shape = RoundedCornerShape(34.dp),
                    color = Color(0xFF222A38),
                    border = BorderStroke(1.2.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Snooze,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Snooze · ${alarm.snoozeDurationMinutes} min",
                            color = Color(0xFFF1F5F9),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Slide to Dismiss Control
                ChronaSlideToDismiss(
                    onDismiss = onDismiss,
                    accentColor = accent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Pixel / Material UI Scalloped 12-Lobed Clock (matching step_1750_media_1.png)
 */
@Composable
fun PixelScallopedClock(
    targetZonedDateTime: ZonedDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val seconds = targetZonedDateTime.second
    val minutes = targetZonedDateTime.minute
    val hours = targetZonedDateTime.hour

    val hourAngle = ((hours % 12) + minutes / 60f + seconds / 3600f) * 30f
    val minuteAngle = (minutes + seconds / 60f) * 6f

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.94f
        val minR = maxR * 0.92f // Subtle, smooth 12-lobed scallop curves
        val midR = (maxR + minR) / 2f
        val amp = (maxR - minR) / 2f

        // 1. Build smooth 12-lobed scalloped path
        val path = androidx.compose.ui.graphics.Path()
        val numPoints = 120
        for (i in 0..numPoints) {
            val angleRad = (i.toFloat() / numPoints) * 2 * Math.PI
            val r = midR + amp * kotlin.math.cos(12 * angleRad).toFloat()
            val x = center.x + r * kotlin.math.cos(angleRad).toFloat()
            val y = center.y + r * kotlin.math.sin(angleRad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        // 2. Draw scalloped container background (Dark Charcoal #23262D)
        drawPath(
            path = path,
            color = Color(0xFF23262D)
        )

        // 3. Draw subtle border stroke (#3A3F49)
        drawPath(
            path = path,
            color = Color(0xFF3A3F49),
            style = Stroke(width = 1.6f.dp.toPx())
        )

        // 4. Minute Hand (Thick rounded pill, medium grey #6B7280)
        val minRad = Math.toRadians((minuteAngle - 90.0))
        val minLen = maxR * 0.70f
        val minEnd = Offset(
            center.x + (minLen * kotlin.math.cos(minRad)).toFloat(),
            center.y + (minLen * kotlin.math.sin(minRad)).toFloat()
        )
        drawLine(
            color = Color(0xFF6B7280),
            start = center,
            end = minEnd,
            strokeWidth = 12.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 5. Hour Hand (Thick rounded pill, lighter grey/periwinkle #A8B0BC)
        val hourRad = Math.toRadians((hourAngle - 90.0))
        val hourLen = maxR * 0.46f
        val hourEnd = Offset(
            center.x + (hourLen * kotlin.math.cos(hourRad)).toFloat(),
            center.y + (hourLen * kotlin.math.sin(hourRad)).toFloat()
        )
        drawLine(
            color = Color(0xFFA8B0BC),
            start = center,
            end = hourEnd,
            strokeWidth = 12.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 6. Center pivot cap with accent center
        drawCircle(
            color = Color(0xFFA8B0BC),
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = accentColor,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}
