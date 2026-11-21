package com.example.chrona.feature.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
                title = "Timers",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToScreensaver = onNavigateToScreensaver,
                onNavigateToAbout = onNavigateToAbout
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (activeTimers.isEmpty()) {
            // New Timer Layout matching reference image step_1750_media_0.png
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section: Time Display with Hours, Minutes, Seconds labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Hours
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%02d", parsedHours),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (parsedHours > 0) extendedColors.textPrimary else Color(0xFF6B7280),
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )
                        Text(
                            text = "Hours",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(28.dp))
                    // Minutes
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%02d", parsedMins),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (parsedMins > 0 || parsedHours > 0) extendedColors.textPrimary else Color(0xFF6B7280),
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )
                        Text(
                            text = "Minutes",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(28.dp))
                    // Seconds
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%02d", parsedSecs),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (totalInputMillis > 0) extendedColors.textPrimary else Color(0xFF6B7280),
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )
                        Text(
                            text = "Seconds",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }

                // Middle Section: Quick Preset Chips and Start Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preset Chips: 1m, 5m, 10m, 15m
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(1 to "1m", 5 to "5m", 10 to "10m", 15 to "15m").forEach { (min, label) ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = extendedColors.surfaceLow,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable {
                                        val totalSec = min * 60
                                        val h = totalSec / 3600
                                        val m = (totalSec % 3600) / 60
                                        val s = totalSec % 60
                                        inputDigits = String.format(Locale.US, "%02d%02d%02d", h, m, s).trimStart('0')
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = extendedColors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Start Button: Large wide pill
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
                            .height(60.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (totalInputMillis > 0) MaterialTheme.colorScheme.primaryContainer else extendedColors.surfaceLow,
                            contentColor = if (totalInputMillis > 0) MaterialTheme.colorScheme.primary else extendedColors.textSecondary,
                            disabledContainerColor = extendedColors.surfaceLow,
                            disabledContentColor = extendedColors.textMuted
                        )
                    ) {
                        Text(
                            text = "Start",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Bottom Section: 3x4 Numeric Keypad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("00", "0", "BACKSPACE")
                    )

                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            when (key) {
                                                "BACKSPACE" -> if (inputDigits.isNotEmpty()) inputDigits = inputDigits.dropLast(1)
                                                "00" -> if (inputDigits.isNotEmpty() && inputDigits.length <= 4) inputDigits += "00"
                                                "0" -> if (inputDigits.isNotEmpty() && inputDigits.length < 6) inputDigits += "0"
                                                else -> if (inputDigits.length < 6) inputDigits += key
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "BACKSPACE") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = extendedColors.textPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = extendedColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(190.dp),
                    color = extendedColors.surfaceHigh,
                    strokeWidth = 9.dp
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(190.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 9.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timer.formattedRemaining(),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        color = extendedColors.textPrimary,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                    )
                    Text(
                        text = if (timer.isRunning) "Running" else "Paused",
                        fontSize = 13.sp,
                        color = if (timer.isRunning) MaterialTheme.colorScheme.primary else extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(extendedColors.surfaceHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset",
                        tint = extendedColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                FloatingActionButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF0B1320),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timer.isRunning) "Pause" else "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = extendedColors.surfaceHigh,
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable(onClick = onAddOneMinute)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+1:00",
                            color = extendedColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
