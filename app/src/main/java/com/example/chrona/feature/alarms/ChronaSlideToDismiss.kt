package com.example.chrona.feature.alarms

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChronaSlideToDismiss(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val trackHeight = 76.dp
    val thumbSize = 62.dp
    val thumbMargin = 7.dp

    val thumbMarginPx = with(density) { thumbMargin.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }

    val offsetX = remember { Animatable(0f) }
    var hasTriggeredDismiss by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(38.dp))
            .semantics {
                contentDescription = "Slide to dismiss alarm"
                customActions = listOf(
                    CustomAccessibilityAction(label = "Dismiss alarm") {
                        if (!hasTriggeredDismiss) {
                            hasTriggeredDismiss = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            true
                        } else false
                    }
                )
            }
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val maxDragPx = (totalWidthPx - thumbSizePx - (thumbMarginPx * 2)).coerceAtLeast(0f)
        val thresholdPx = maxDragPx * 0.80f

        val dragFraction = if (maxDragPx > 0) (offsetX.value / maxDragPx).coerceIn(0f, 1f) else 0f

        val draggableState = rememberDraggableState { delta ->
            if (!hasTriggeredDismiss) {
                coroutineScope.launch {
                    val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragPx)
                    offsetX.snapTo(newOffset)
                }
            }
        }

        // Capsule track background
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(38.dp),
            color = Color(0xFF1E2638),
            border = BorderStroke(1.2.dp, Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Directional hint label that fades as thumb advances
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Text(
                        text = "Slide to dismiss",
                        color = Color(0xFFF1F5F9).copy(alpha = (1f - (dragFraction * 1.5f)).coerceIn(0f, 1f)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Draggable thumb pill
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = thumbMargin),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                            .size(thumbSize)
                            .clip(CircleShape)
                            .background(accentColor)
                            .draggable(
                                state = draggableState,
                                orientation = Orientation.Horizontal,
                                onDragStopped = {
                                    if (offsetX.value >= thresholdPx && !hasTriggeredDismiss) {
                                        hasTriggeredDismiss = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        coroutineScope.launch {
                                            offsetX.animateTo(
                                                targetValue = maxDragPx,
                                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                            )
                                            onDismiss()
                                        }
                                    } else if (!hasTriggeredDismiss) {
                                        coroutineScope.launch {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    stiffness = Spring.StiffnessMediumLow,
                                                    dampingRatio = Spring.DampingRatioLowBouncy
                                                )
                                            )
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner dark circular icon with arrow
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B1320)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFF1F5F9),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
