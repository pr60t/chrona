package com.example.chrona.feature.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.model.TimerItem
import com.example.chrona.core.ui.components.ChronaTopBar
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.util.Locale

@Composable
fun TimerScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val activeTimers by TimerManager.timers.collectAsState()

    // Keypad input state in digits (e.g. "0500" for 5 min)
    var inputDigits by remember { mutableStateOf("") }

    val parsedHours: Int
    val parsedMins: Int
    val parsedSecs: Int
    val padded = inputDigits.padStart(6, '0')
    parsedHours = padded.substring(0, 2).toIntOrNull() ?: 0
    parsedMins = padded.substring(2, 4).toIntOrNull() ?: 0
    parsedSecs = padded.substring(4, 6).toIntOrNull() ?: 0
    val totalInputMillis = (parsedHours * 3600L + parsedMins * 60L + parsedSecs) * 1000L

    Scaffold(
        topBar = {
            ChronaTopBar(
                title = "Timer",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToScreensaver = onNavigateToScreensaver,
                onNavigateToAbout = onNavigateToAbout
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (activeTimers.isEmpty()) {
            // Timer Home: Keypad & Presets (timer_home.svg)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Large formatted display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = String.format(Locale.US, "%02d:%02d:%02d", parsedHours, parsedMins, parsedSecs),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = if (totalInputMillis > 0) extendedColors.textPrimary else extendedColors.textMuted,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "1m", 5 to "5m", 10 to "10m", 15 to "15m").forEach { (min, label) ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = extendedColors.surfaceHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable {
                                        TimerManager.addTimer(min * 60 * 1000L, "$min min")
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$label",
                                        color = extendedColors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Numeric Keypad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("00", "0", "⌫")
                    )

                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { key ->
                                Surface(
                                    shape = CircleShape,
                                    color = extendedColors.surfaceLow,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.8f)
                                        .clip(CircleShape)
                                        .clickable {
                                            when (key) {
                                                "⌫" -> if (inputDigits.isNotEmpty()) inputDigits = inputDigits.dropLast(1)
                                                "00" -> if (inputDigits.length <= 4) inputDigits += "00"
                                                else -> if (inputDigits.length < 6) inputDigits += key
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = extendedColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Start button
                Button(
                    onClick = {
                        if (totalInputMillis > 0) {
                            TimerManager.addTimer(totalInputMillis, "Timer")
                            inputDigits = ""
                        }
                    },
                    enabled = totalInputMillis > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color(0xFF0B1320),
                        disabledContainerColor = extendedColors.surfaceLow,
                        disabledContentColor = extendedColors.textMuted
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start timer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Active / Multiple Timers View (timer_active.svg, timer_multiple.svg)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activeTimers, key = { it.id }) { timer ->
                    TimerCard(
                        timer = timer,
                        onTogglePlayPause = { TimerManager.togglePlayPause(timer.id) },
                        onAddOneMinute = { TimerManager.addOneMinute(timer.id) },
                        onReset = { TimerManager.resetTimer(timer.id) },
                        onDelete = { TimerManager.removeTimer(timer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerCard(
    timer: TimerItem,
    onTogglePlayPause: () -> Unit,
    onAddOneMinute: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val animatedProgress by animateFloatAsState(targetValue = timer.progress, label = "timerProgress")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.cardBackground
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timer.label,
                    color = extendedColors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Circular progress ring with big tabular time inside
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = extendedColors.surfaceHigh,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (timer.isFinished) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timer.formattedRemaining(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                        color = if (timer.isFinished) Color(0xFFFCA5A5) else extendedColors.textPrimary,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                    )
                    if (timer.isFinished) {
                        Text(
                            text = "Finished!",
                            color = Color(0xFFFCA5A5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls row: +1:00, Play/Pause, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // +1:00 min pill
                OutlinedButton(
                    onClick = onAddOneMinute,
                    shape = RoundedCornerShape(100.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.outline)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1:00", fontSize = 13.sp)
                }

                // Play / Pause pill
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timer.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Reset
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset",
                        tint = extendedColors.textSecondary
                    )
                }
            }
        }
    }
}
