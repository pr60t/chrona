package com.example.chrona.feature.screensaver

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.AlarmRepository
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.core.timezone.TimezoneEngine
import com.example.chrona.ui.theme.ChronaDarkBg
import com.example.chrona.ui.theme.TABULAR_NUMS
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ScreensaverScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by PreferencesRepository.preferences.collectAsState()
    val alarms by AlarmRepository.alarms.collectAsState()

    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Instant.now()
        }
    }

    // Slow drifting animation to prevent OLED burn-in
    val driftTransition = rememberInfiniteTransition(label = "burnInDrift")
    val driftX by driftTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by driftTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 45_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    val timeFormatter = DateTimeFormatter.ofPattern(
        if (preferences.is24HourFormat) "HH:mm" else "h:mm",
        Locale.getDefault()
    )
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

    val timeStr = now.atZone(java.time.ZoneId.systemDefault()).format(timeFormatter)
    val amPmStr = if (!preferences.is24HourFormat) {
        now.atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("a", Locale.getDefault())).uppercase()
    } else ""
    val dateStr = now.atZone(java.time.ZoneId.systemDefault()).format(dateFormatter)

    // Find next upcoming alarm
    val nextAlarm = remember(alarms, now) {
        alarms.filter { it.isEnabled }
            .mapNotNull { alarm ->
                TimezoneEngine.calculateNextTriggerInstant(alarm, now)?.let { instant ->
                    Pair(alarm, instant)
                }
            }
            .minByOrNull { it.second }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChronaDarkBg)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(driftX.roundToInt(), driftY.roundToInt()) }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeStr,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFFF1F5F9).copy(alpha = 0.85f),
                    style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                )
                if (amPmStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amPmStr,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8).copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dateStr,
                fontSize = 16.sp,
                color = Color(0xFF94A3B8).copy(alpha = 0.75f),
                fontWeight = FontWeight.Normal
            )

            if (nextAlarm != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF93A8C7).copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${nextAlarm.first.formattedTargetTime(preferences.is24HourFormat)} (${nextAlarm.first.targetCityName})",
                        fontSize = 13.sp,
                        color = Color(0xFF93A8C7).copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Tap anywhere to exit",
                fontSize = 12.sp,
                color = Color(0xFF64748B).copy(alpha = 0.5f)
            )
        }
    }
}
