package com.example.chrona.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.model.City
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Instant
import java.time.ZoneId

@Composable
fun TimezoneCard(
    city: City,
    is24Hour: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showImage: Boolean = false, // Ignored, kept for backward compatibility
    deviceZoneId: ZoneId = ZoneId.systemDefault(),
    referenceInstant: Instant = Instant.now()
) {
    val extendedColors = ChronaThemeTokens.extended
    val isDay = city.isDaytime(referenceInstant)
    val weekday = city.formattedWeekday(referenceInstant)
    val formattedTime = city.formattedTime(is24Hour, referenceInstant)
    val amPm = city.amPm(referenceInstant)
    val offset = city.formattedOffset(referenceInstant)
    val timeDiff = city.relativeTimeDifference(deviceZoneId, referenceInstant)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.cardBackground,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 4dp vertical pastel identity strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(city.identityColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left section: City details & offsets (clean, balanced layout)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = city.cityName,
                        color = extendedColors.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (city.countryName.isNotEmpty()) city.countryName else city.admin1Name,
                        color = extendedColors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badge row: Local Weekday + UTC offset + Relative time difference
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = extendedColors.surfaceHigh
                        ) {
                            Text(
                                text = weekday,
                                color = extendedColors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = extendedColors.surfaceHigh
                        ) {
                            Text(
                                text = offset,
                                color = extendedColors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "•",
                            color = extendedColors.textMuted,
                            fontSize = 10.sp
                        )

                        Text(
                            text = timeDiff,
                            color = extendedColors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Right section: Large live tabular clock + Day/Night indicator
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = formattedTime,
                            color = extendedColors.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                        )

                        if (!is24Hour) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = amPm,
                                color = extendedColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day/Night indicator badge (or Standard for UTC without geographic coordinates)
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.surfaceHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isDay != null) {
                                Icon(
                                    imageVector = if (isDay) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                    contentDescription = if (isDay) "Day" else "Night",
                                    tint = if (isDay) Color(0xFFFCD34D) else Color(0xFF96A8E8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isDay) "Day" else "Night",
                                    color = extendedColors.textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = "Standard Time",
                                    tint = extendedColors.textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Standard",
                                    color = extendedColors.textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
