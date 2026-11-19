package com.example.chrona.feature.stopwatch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StopwatchScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val isRunning by StopwatchManager.isRunning.collectAsState()
    val laps by StopwatchManager.laps.collectAsState()

    var elapsedMillis by remember { mutableLongStateOf(StopwatchManager.getElapsedMillis()) }

    // High-frequency UI ticker (16ms ~ 60fps) when on this screen and running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                elapsedMillis = StopwatchManager.getElapsedMillis()
                delay(16)
            }
        } else {
            elapsedMillis = StopwatchManager.getElapsedMillis()
        }
    }

    val minutes = elapsedMillis / 60000
    val seconds = (elapsedMillis % 60000) / 1000
    val hundredths = (elapsedMillis % 1000) / 10

    // Sweep hand angle: 360 degrees every 60 seconds
    val sweepAngle = ((elapsedMillis % 60000) / 60000f) * 360f

    // Find fastest and slowest lap
    val fastestLapTime = remember(laps.size) {
        if (laps.size >= 2) laps.minOf { it.lapTimeMillis } else null
    }
    val slowestLapTime = remember(laps.size) {
        if (laps.size >= 2) laps.maxOf { it.lapTimeMillis } else null
    }

    Scaffold(
        topBar = {
            ChronaTopBar(
                title = "Stopwatch",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToScreensaver = onNavigateToScreensaver,
                onNavigateToAbout = onNavigateToAbout
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Region 1: Centered Chronograph Dial & Time Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    val dialSize = minOf(maxWidth, maxHeight, 300.dp)
                    val tickColor = extendedColors.outline
                    val primaryColor = MaterialTheme.colorScheme.primary

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(dialSize)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = size.minDimension / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)

                            // 60 precision ticks
                            for (i in 0 until 60) {
                                val angleDeg = (i * 6f) - 90f
                                val angleRad = Math.toRadians(angleDeg.toDouble())
                                val isMajor = i % 5 == 0
                                val tickLength = if (isMajor) 14.dp.toPx() else 6.dp.toPx()
                                val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                                val startX = center.x + ((radius - tickLength) * cos(angleRad)).toFloat()
                                val startY = center.y + ((radius - tickLength) * sin(angleRad)).toFloat()
                                val endX = center.x + (radius * cos(angleRad)).toFloat()
                                val endY = center.y + (radius * sin(angleRad)).toFloat()

                                drawLine(
                                    color = if (isMajor) tickColor else tickColor.copy(alpha = 0.45f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = tickWidth,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Animated Sweep Hand
                            val sweepRad = Math.toRadians((sweepAngle - 90.0))
                            val handLength = radius - 16.dp.toPx()
                            val handEndX = center.x + (handLength * cos(sweepRad)).toFloat()
                            val handEndY = center.y + (handLength * sin(sweepRad)).toFloat()

                            drawLine(
                                color = primaryColor,
                                start = center,
                                end = Offset(handEndX, handEndY),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Pivot Dot
                            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)
                            drawCircle(color = Color(0xFF000000), radius = 2.dp.toPx(), center = center)
                        }

                        // Digital Readout in center
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Light,
                                    color = extendedColors.textPrimary,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                )
                                Text(
                                    text = String.format(Locale.US, ".%02d", hundredths),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Region 2: Independently Scrollable Lap Records List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.85f)
                    .padding(horizontal = 24.dp)
            ) {
                if (laps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (elapsedMillis > 0) "Paused" else "Ready",
                            color = extendedColors.textMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(laps, key = { _, lap -> lap.lapIndex }) { _, lap ->
                            val isFastest = fastestLapTime != null && lap.lapTimeMillis == fastestLapTime
                            val isSlowest = slowestLapTime != null && lap.lapTimeMillis == slowestLapTime

                            val lapColor = when {
                                isFastest -> Color(0xFF6EE7B7) // Green highlight
                                isSlowest -> Color(0xFFFCA5A5) // Red highlight
                                else -> extendedColors.textPrimary
                            }

                            val lapMins = lap.lapTimeMillis / 60000
                            val lapSecs = (lap.lapTimeMillis % 60000) / 1000
                            val lapHundredths = (lap.lapTimeMillis % 1000) / 10

                            val totalMins = lap.totalTimeMillis / 60000
                            val totalSecs = (lap.totalTimeMillis % 60000) / 1000
                            val totalHundredths = (lap.totalTimeMillis % 1000) / 10

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = extendedColors.cardBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Lap ${lap.lapIndex}",
                                        color = extendedColors.textSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = String.format(Locale.US, "+%02d:%02d.%02d", lapMins, lapSecs, lapHundredths),
                                        color = lapColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                    )

                                    Text(
                                        text = String.format(Locale.US, "%02d:%02d.%02d", totalMins, totalSecs, totalHundredths),
                                        color = extendedColors.textMuted,
                                        fontSize = 13.sp,
                                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Region 3: Stable Anchored Bottom Controls (Reset | Start/Pause | Lap)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button (Left)
                    IconButton(
                        onClick = {
                            StopwatchManager.reset()
                            elapsedMillis = 0L
                        },
                        enabled = elapsedMillis > 0 && !isRunning,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Reset",
                            tint = if (elapsedMillis > 0 && !isRunning) extendedColors.textPrimary else extendedColors.textMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Prominent Start / Pause Button (Center, 68dp)
                    FilledIconButton(
                        onClick = {
                            if (isRunning) {
                                StopwatchManager.pause()
                            } else {
                                StopwatchManager.start()
                            }
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isRunning) MaterialTheme.colorScheme.primary else Color(0xFF0B1320)
                        ),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Lap Button (Right)
                    Button(
                        onClick = {
                            StopwatchManager.recordLap()
                        },
                        enabled = isRunning,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.surfaceHigh,
                            contentColor = extendedColors.textPrimary,
                            disabledContainerColor = extendedColors.surfaceLow,
                            disabledContentColor = extendedColors.textMuted.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Lap",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
