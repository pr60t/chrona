package com.example.chrona.feature.alarms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class TimePickerSelectionMode {
    HOUR,
    MINUTE
}

@Composable
fun ChronaRadialTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean = false,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ChronaThemeTokens.extended
    val isDark = ChronaThemeTokens.isDark

    var currentHour by remember { mutableStateOf(initialHour) }
    var currentMinute by remember { mutableStateOf(initialMinute) }
    var selectionMode by remember { mutableStateOf(TimePickerSelectionMode.HOUR) }

    var isAm by remember { mutableStateOf(initialHour < 12) }

    val displayHour = remember(currentHour, is24Hour) {
        if (is24Hour) currentHour else {
            when {
                currentHour == 0 -> 12
                currentHour > 12 -> currentHour - 12
                else -> currentHour
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(372.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (isDark) Color(0xFF2B3547) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title
                Text(
                    text = if (is24Hour) "24-HOUR CLOCK · DUAL RING SELECTION" else "12-HOUR CLOCK · HOUR & MINUTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Digital Readout Box Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour Box
                    val isHourSelected = selectionMode == TimePickerSelectionMode.HOUR
                    Surface(
                        modifier = Modifier
                            .size(width = if (is24Hour) 130.dp else 100.dp, height = 76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectionMode = TimePickerSelectionMode.HOUR },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isHourSelected) {
                            if (isDark) Color(0xFF2A384D) else Color(0xFFDCE6F5)
                        } else {
                            if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8)
                        },
                        border = if (isHourSelected) {
                            BorderStroke(1.6.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = String.format(Locale.US, "%02d", displayHour),
                                fontSize = 42.sp,
                                fontWeight = if (isHourSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isHourSelected) {
                                    if (isDark) Color(0xFFDCE6F7) else Color(0xFF1E3352)
                                } else {
                                    if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                },
                                style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                            )
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 36.sp,
                        color = extendedColors.textMuted,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute Box
                    val isMinuteSelected = selectionMode == TimePickerSelectionMode.MINUTE
                    Surface(
                        modifier = Modifier
                            .size(width = if (is24Hour) 130.dp else 100.dp, height = 76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectionMode = TimePickerSelectionMode.MINUTE },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMinuteSelected) {
                            if (isDark) Color(0xFF2A384D) else Color(0xFFDCE6F5)
                        } else {
                            if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8)
                        },
                        border = if (isMinuteSelected) {
                            BorderStroke(1.6.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = String.format(Locale.US, "%02d", currentMinute),
                                fontSize = 42.sp,
                                fontWeight = if (isMinuteSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isMinuteSelected) {
                                    if (isDark) Color(0xFFDCE6F7) else Color(0xFF1E3352)
                                } else {
                                    if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                },
                                style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                            )
                        }
                    }

                    // AM / PM Stacked Capsules (12h mode only)
                    if (!is24Hour) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // AM Capsule
                            Surface(
                                modifier = Modifier
                                    .size(width = 68.dp, height = 35.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isAm = true
                                        if (currentHour >= 12) currentHour -= 12
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAm) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8)
                                },
                                border = if (!isAm) BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "AM",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAm) {
                                            if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                        } else {
                                            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                                        }
                                    )
                                }
                            }

                            // PM Capsule
                            Surface(
                                modifier = Modifier
                                    .size(width = 68.dp, height = 35.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isAm = false
                                        if (currentHour < 12) currentHour += 12
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isAm) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8)
                                },
                                border = if (isAm) BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "PM",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAm) {
                                            if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                        } else {
                                            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Radial Dial Canvas
                val dialSizeDp = 244.dp
                val density = LocalDensity.current
                val dialRadiusPx = with(density) { (dialSizeDp / 2).toPx() }

                Box(
                    modifier = Modifier
                        .size(dialSizeDp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8))
                        .pointerInput(selectionMode, is24Hour, isAm) {
                            fun handleTouch(pos: Offset) {
                                val dx = pos.x - dialRadiusPx
                                val dy = pos.y - dialRadiusPx
                                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (angleDeg < 0) angleDeg += 360f

                                if (selectionMode == TimePickerSelectionMode.HOUR) {
                                    if (is24Hour) {
                                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                        val isOuter = dist > dialRadiusPx * 0.65f
                                        val step = ((angleDeg / 30f).roundToInt()) % 12
                                        currentHour = if (isOuter) step else step + 12
                                    } else {
                                        var rawHour = ((angleDeg / 30f).roundToInt()) % 12
                                        if (rawHour == 0) rawHour = 12
                                        currentHour = if (isAm) {
                                            if (rawHour == 12) 0 else rawHour
                                        } else {
                                            if (rawHour == 12) 12 else rawHour + 12
                                        }
                                    }
                                } else {
                                    val rawMin = ((angleDeg / 6f).roundToInt()) % 60
                                    currentMinute = rawMin
                                }
                            }

                            detectTapGestures { offset ->
                                handleTouch(offset)
                                if (selectionMode == TimePickerSelectionMode.HOUR) {
                                    selectionMode = TimePickerSelectionMode.MINUTE
                                }
                            }
                        }
                        .pointerInput(selectionMode, is24Hour, isAm) {
                            detectDragGestures { change, _ ->
                                val dx = change.position.x - dialRadiusPx
                                val dy = change.position.y - dialRadiusPx
                                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (angleDeg < 0) angleDeg += 360f

                                if (selectionMode == TimePickerSelectionMode.HOUR) {
                                    if (is24Hour) {
                                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                        val isOuter = dist > dialRadiusPx * 0.65f
                                        val step = ((angleDeg / 30f).roundToInt()) % 12
                                        currentHour = if (isOuter) step else step + 12
                                    } else {
                                        var rawHour = ((angleDeg / 30f).roundToInt()) % 12
                                        if (rawHour == 0) rawHour = 12
                                        currentHour = if (isAm) {
                                            if (rawHour == 12) 0 else rawHour
                                        } else {
                                            if (rawHour == 12) 12 else rawHour + 12
                                        }
                                    }
                                } else {
                                    val rawMin = ((angleDeg / 6f).roundToInt()) % 60
                                    currentMinute = rawMin
                                }
                            }
                        }
                ) {
                    val activeColor = MaterialTheme.colorScheme.primary

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val outerRadius = size.width * 0.40f
                        val innerRadius = size.width * 0.24f

                        // Determine target angle and radius for current selected value
                        val (targetAngleDeg, targetRadius) = if (selectionMode == TimePickerSelectionMode.HOUR) {
                            if (is24Hour) {
                                val isInner = currentHour >= 12
                                val step = currentHour % 12
                                Pair(step * 30f, if (isInner) innerRadius else outerRadius)
                            } else {
                                val h = if (displayHour == 12) 0 else displayHour
                                Pair(h * 30f, outerRadius)
                            }
                        } else {
                            Pair(currentMinute * 6f, outerRadius)
                        }

                        val targetAngleRad = Math.toRadians((targetAngleDeg - 90f).toDouble())
                        val selectorCenter = Offset(
                            (center.x + targetRadius * cos(targetAngleRad)).toFloat(),
                            (center.y + targetRadius * sin(targetAngleRad)).toFloat()
                        )

                        // Central pin
                        drawCircle(color = activeColor, radius = 4.dp.toPx(), center = center)

                        // Sweep arm line
                        drawLine(
                            color = activeColor,
                            start = center,
                            end = selectorCenter,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Selector highlight disc
                        drawCircle(color = activeColor, radius = 18.dp.toPx(), center = selectorCenter)
                    }

                    // Render Numbers around the dial
                    if (selectionMode == TimePickerSelectionMode.HOUR) {
                        if (is24Hour) {
                            // Outer ring: 00 to 11
                            for (i in 0..11) {
                                val angleRad = Math.toRadians((i * 30f - 90f).toDouble())
                                val r = dialRadiusPx * 0.80f
                                val x = dialRadiusPx + (r * cos(angleRad)).toFloat()
                                val y = dialRadiusPx + (r * sin(angleRad)).toFloat()
                                val isSelected = currentHour == i

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset((x - 14.dp.toPx()).roundToInt(), (y - 14.dp.toPx()).roundToInt()) }
                                        .size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%02d", i),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) {
                                            if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                        } else {
                                            if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                        }
                                    )
                                }
                            }

                            // Inner ring: 12 to 23
                            for (i in 12..23) {
                                val angleRad = Math.toRadians(((i - 12) * 30f - 90f).toDouble())
                                val r = dialRadiusPx * 0.48f
                                val x = dialRadiusPx + (r * cos(angleRad)).toFloat()
                                val y = dialRadiusPx + (r * sin(angleRad)).toFloat()
                                val isSelected = currentHour == i

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset((x - 14.dp.toPx()).roundToInt(), (y - 14.dp.toPx()).roundToInt()) }
                                        .size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = i.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) {
                                            if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                        } else {
                                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        }
                                    )
                                }
                            }
                        } else {
                            // 12-Hour dial: 1 to 12
                            for (i in 1..12) {
                                val angleRad = Math.toRadians((i * 30f - 90f).toDouble())
                                val r = dialRadiusPx * 0.78f
                                val x = dialRadiusPx + (r * cos(angleRad)).toFloat()
                                val y = dialRadiusPx + (r * sin(angleRad)).toFloat()
                                val isSelected = displayHour == i

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset((x - 14.dp.toPx()).roundToInt(), (y - 14.dp.toPx()).roundToInt()) }
                                        .size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = i.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) {
                                            if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                        } else {
                                            if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Minute dial: 00, 05, 10, ... 55
                        for (i in 0..11) {
                            val minVal = i * 5
                            val angleRad = Math.toRadians((minVal * 6f - 90f).toDouble())
                            val r = dialRadiusPx * 0.78f
                            val x = dialRadiusPx + (r * cos(angleRad)).toFloat()
                            val y = dialRadiusPx + (r * sin(angleRad)).toFloat()
                            val isSelected = currentMinute == minVal

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset((x - 14.dp.toPx()).roundToInt(), (y - 14.dp.toPx()).roundToInt()) }
                                    .size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%02d", minVal),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                                    } else {
                                        if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Status Line & Action Buttons
                val timeSummary = if (is24Hour) {
                    String.format(Locale.US, "Selected: %02d:%02d", currentHour, currentMinute)
                } else {
                    String.format(Locale.US, "Selected: %02d:%02d %s", displayHour, currentMinute, if (isAm) "AM" else "PM")
                }
                Text(
                    text = timeSummary,
                    fontSize = 11.5.sp,
                    color = extendedColors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .clickable { onDismiss() },
                        shape = RoundedCornerShape(21.dp),
                        color = if (isDark) Color(0xFF1A202C) else Color(0xFFF0F3F8)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // OK Button
                    val confirmAccent = MaterialTheme.colorScheme.primary
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .clickable {
                                val finalHour = if (is24Hour) currentHour else {
                                    if (isAm) {
                                        if (displayHour == 12) 0 else displayHour
                                    } else {
                                        if (displayHour == 12) 12 else displayHour + 12
                                    }
                                }
                                onTimeSelected(finalHour, currentMinute)
                            },
                        shape = RoundedCornerShape(21.dp),
                        color = confirmAccent
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "OK",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF0B1320) else Color(0xFFFFFFFF)
                            )
                        }
                    }
                }
            }
        }
    }
}
