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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val timeStr = targetZdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    val fullDateStr = targetZdt.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))

    val localPreview = remember(now) {
        TimezoneEngine.previewLocalTime(alarm, referenceInstant = now)
    }

    val pulseTransition = rememberInfiniteTransition(label = "ringingPulse")
    val pulseRingRadius by pulseTransition.animateFloat(
        initialValue = 120f,
        targetValue = 158f,
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
        // Soft ambient radial glow and concentric ripple rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.35f)

            // Radial gradient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.22f),
                        Color(0xFF93A8C7).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.7f
                ),
                center = centerOffset,
                radius = size.width * 0.7f
            )

            // Outer subtle concentric ring
            drawCircle(
                color = accent.copy(alpha = 0.16f),
                radius = pulseRingRadius * density,
                center = centerOffset,
                style = Stroke(width = 1.2f * density)
            )

            // Inner subtle concentric ring
            drawCircle(
                color = accent.copy(alpha = pulseAlpha),
                radius = (pulseRingRadius - 32f) * density,
                center = centerOffset,
                style = Stroke(width = 1.5f * density)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Timezone / City pill and Alarm Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

            // MIDDLE SECTION: Hero Destination Clock + Local Secondary Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeStr,
                    color = Color(0xFFF1F5F9),
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp,
                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                )

                Text(
                    text = fullDateStr,
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                if (alarm.isCrossTimezone) {
                    Spacer(modifier = Modifier.height(28.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1A202C),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFF2DF9B))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DEVICE LOCAL · YOUR CURRENT TIME",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${localPreview.deviceTime} ${localPreview.deviceAmPm}",
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${localPreview.dayOffsetDescription})",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM SECTION: Large accessible Snooze (68dp) + Dismiss Pill (84dp)
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
                            tint = Color(0xFF93A8C7),
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

                // Primary Unambiguous Dismiss Pill (Height 84dp, rx 42dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(42.dp))
                        .clickable { onDismiss() },
                    shape = RoundedCornerShape(42.dp),
                    color = Color(0xFF93A8C7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dark icon circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B1320)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFF1F5F9),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Text(
                            text = "Dismiss alarm",
                            color = Color(0xFF0B1320),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Spacer to balance the left circle
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                }
            }
        }
    }
}
