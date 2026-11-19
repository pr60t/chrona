package com.example.chrona.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.ui.theme.ChronaThemeTokens
import kotlin.math.abs

enum class NavDestination(val label: String, val icon: ImageVector) {
    ALARMS("Alarms", Icons.Outlined.Alarm),
    WORLD_CLOCK("World Clock", Icons.Outlined.Public),
    TIMER("Timer", Icons.Outlined.HourglassBottom),
    STOPWATCH("Stopwatch", Icons.Outlined.Timer),
    BEDTIME("Bedtime", Icons.Outlined.Bedtime)
}

@Composable
fun ChronaBottomBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val destinations = NavDestination.entries
    val targetIndex = destinations.indexOf(currentDestination).coerceAtLeast(0)

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // Shared continuous animated indicator center position (0f .. 4f)
    val animatedSlotIndex = remember { Animatable(targetIndex.toFloat()) }

    LaunchedEffect(targetIndex) {
        animatedSlotIndex.animateTo(
            targetValue = targetIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = 0.76f, // Restrained physical bounce
                stiffness = 380f     // Responsive and fluid
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column {
            // Subtle top divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(extendedColors.divider.copy(alpha = 0.5f))
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                val totalWidth = maxWidth
                val slotWidth = totalWidth / destinations.size

                // Physical bubble stretch calculation based on instantaneous travel velocity
                val velocity = animatedSlotIndex.velocity
                val stretchFactor = (abs(velocity) * 0.08f).coerceIn(0f, 0.35f)
                val restingPillWidth = 56.dp
                val pillHeight = 32.dp
                val animatedPillWidth = restingPillWidth * (1f + stretchFactor)

                // Effective slot fraction considering RTL
                val currentSlotFraction = if (isRtl) {
                    (destinations.size - 1) - animatedSlotIndex.value
                } else {
                    animatedSlotIndex.value
                }

                val indicatorCenterX = slotWidth * (currentSlotFraction + 0.5f)
                val indicatorLeft = indicatorCenterX - (animatedPillWidth / 2f)

                // The single continuous shared highlight bubble
                Box(
                    modifier = Modifier
                        .offset(x = indicatorLeft, y = 10.dp)
                        .size(width = animatedPillWidth, height = pillHeight)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )

                // Navigation item slots
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.forEach { destination ->
                        val isSelected = destination == currentDestination
                        val interactionSource = remember { MutableInteractionSource() }

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textMuted,
                            animationSpec = tween(durationMillis = 200),
                            label = "contentColor"
                        )

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "iconScale"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                }
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (currentDestination != destination) {
                                        onNavigate(destination)
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(pillHeight)
                                    .width(restingPillWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    tint = contentColor,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(iconScale)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = destination.label,
                                color = contentColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
