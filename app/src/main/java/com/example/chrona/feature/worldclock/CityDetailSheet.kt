package com.example.chrona.feature.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.model.City
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailSheet(
    city: City,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onSetAlarmForCity: (City) -> Unit,
    onToggleSaveCity: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var now by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = Instant.now()
        }
    }

    val isDay = city.isDaytime(now)
    val formattedTime = city.formattedTime(is24Hour, now)
    val amPm = city.amPm(now)
    val offset = city.formattedOffset(now)
    val deviceZoneId = ZoneId.systemDefault()
    val timeDiff = city.relativeTimeDifference(deviceZoneId, now)
    val deviceLocalTime = now.atZone(deviceZoneId).format(
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = extendedColors.cardBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(extendedColors.outline)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // City Header & Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(city.identityColor)
                    )
                    Column {
                        Text(
                            text = city.cityName,
                            color = extendedColors.textPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${city.countryName} · ${city.zoneIdString}",
                            color = extendedColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleSaveCity(city.geonameId) }
                ) {
                    Icon(
                        imageVector = if (city.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save city",
                        tint = if (city.isSaved) city.identityColor else extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Live Clock Surface
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = extendedColors.surfaceHigh
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formattedTime,
                            color = extendedColors.textPrimary,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Light,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )
                        if (!is24Hour) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = amPm,
                                color = city.identityColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = city.formattedFullDate(now),
                        color = extendedColors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.surfaceHighest
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isDay != null) {
                                    Icon(
                                        imageVector = if (isDay) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDay) Color(0xFFFCD34D) else Color(0xFF96A8E8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isDay) "Daytime" else "Night",
                                        color = extendedColors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = extendedColors.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Standard Time",
                                        color = extendedColors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = extendedColors.surfaceHighest
                        ) {
                            Text(
                                text = offset,
                                color = extendedColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = extendedColors.surfaceHighest
                        ) {
                            Text(
                                text = timeDiff,
                                color = extendedColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time difference comparison card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = extendedColors.surfaceLowest
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Time Comparison",
                            color = extendedColors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Right now in your local time (${deviceZoneId.id.substringAfterLast('/')}): $deviceLocalTime",
                            color = extendedColors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action: Set alarm in [City] time
            Button(
                onClick = {
                    onDismiss()
                    onSetAlarmForCity(city)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = city.identityColor,
                    contentColor = Color(0xFF0B1320)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlarm,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Set alarm in ${city.cityName} time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
