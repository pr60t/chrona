package com.example.chrona.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

                // The single continuous shared highlight bubble perfectly centered vertically around the 22dp icon
                // Icon Box is centered in the 72dp column above the label, starting at exactly y = 6.dp
                Box(
                    modifier = Modifier
                        .offset(x = indicatorLeft, y = 6.dp)
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
                                ChronaAnimatedNavIcon(
                                    destination = destination,
                                    isSelected = isSelected,
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

@Composable
private fun ChronaAnimatedNavIcon(
    destination: NavDestination,
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    // 1. Alarms: ringing bell oscillation (-16° to +16° settling)
    val bellRotation = remember { Animatable(0f) }
    // 2. World Clock: 3D realistic spinning globe around vertical axis
    val globeRotation = remember { Animatable(0f) }
    // 3. Timer: upright resting with lower fill, 180° flip + sand flow into lower chamber on entry
    val hourglassFlip = remember { Animatable(0f) }
    val sandProgress = remember { Animatable(0f) } // 0f = resting state with lower chamber filled
    // 4. Stopwatch: stable casing with internal needle spinning 360°
    val needleAngle = remember { Animatable(0f) }
    // 5. Bedtime: stable moon with floating, fading 'Z' glyphs
    val zProgress = remember { Animatable(0f) }

    var lastSelected by remember { mutableStateOf(isSelected) }

    LaunchedEffect(isSelected) {
        val justEntered = isSelected && !lastSelected
        lastSelected = isSelected

        if (isSelected) {
            when (destination) {
                NavDestination.ALARMS -> {
                    if (justEntered) {
                        bellRotation.snapTo(0f)
                        bellRotation.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 440
                                0f at 0
                                (-16f) at 70
                                14f at 140
                                (-10f) at 220
                                6f at 300
                                (-3f) at 370
                                0f at 440
                            }
                        )
                    }
                }
                NavDestination.WORLD_CLOCK -> {
                    if (justEntered) {
                        globeRotation.snapTo(0f)
                        globeRotation.animateTo(
                            targetValue = 360f,
                            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                        )
                    }
                }
                NavDestination.TIMER -> {
                    if (justEntered) {
                        hourglassFlip.snapTo(0f)
                        sandProgress.snapTo(0f)
                        launch {
                            // 1. Exactly one 180° flip (does not rotate back)
                            hourglassFlip.animateTo(
                                targetValue = 180f,
                                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                            )
                            // 2. Sand drains downward through center neck into lower chamber
                            sandProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                }
                NavDestination.STOPWATCH -> {
                    if (justEntered) {
                        needleAngle.snapTo(0f)
                        needleAngle.animateTo(
                            targetValue = 360f,
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                        )
                    }
                }
                NavDestination.BEDTIME -> {
                    if (justEntered) {
                        zProgress.snapTo(0f)
                        zProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing)
                        )
                    }
                }
            }
        } else {
            bellRotation.snapTo(0f)
            globeRotation.snapTo(0f)
            hourglassFlip.snapTo(0f)
            sandProgress.snapTo(0f) // Always resting with lower chamber filled when unselected
            needleAngle.snapTo(0f)
            zProgress.snapTo(0f)
        }
    }

    when (destination) {
        NavDestination.ALARMS -> {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = tint,
                modifier = modifier.graphicsLayer {
                    rotationZ = bellRotation.value
                }
            )
        }
        NavDestination.WORLD_CLOCK -> {
            Canvas(modifier = modifier.size(22.dp)) {
                val r = size.minDimension / 2f * 0.88f
                val c = Offset(size.width / 2f, size.height / 2f)
                val strokeW = 1.4.dp.toPx()
                val tiltRad = Math.toRadians(14.0) // 14° axial tilt
                val cosTilt = kotlin.math.cos(tiltRad).toFloat()
                val sinTilt = kotlin.math.sin(tiltRad).toFloat()

                // Outline circle
                drawCircle(
                    color = tint,
                    radius = r,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                )

                val clipPath = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(c.x - r, c.y - r, c.x + r, c.y + r))
                }

                clipPath(clipPath) {
                    fun project(latDeg: Float, lonRad: Double): Triple<Float, Float, Float> {
                        val beta = Math.toRadians(latDeg.toDouble())
                        val cosBeta = kotlin.math.cos(beta).toFloat()
                        val sinBeta = kotlin.math.sin(beta).toFloat()
                        val sinPhi = kotlin.math.sin(lonRad).toFloat()
                        val cosPhi = kotlin.math.cos(lonRad).toFloat()

                        val x0 = cosBeta * sinPhi
                        val y0 = sinBeta
                        val z0 = cosBeta * cosPhi

                        val x = x0
                        val y = y0 * cosTilt - z0 * sinTilt
                        val z = y0 * sinTilt + z0 * cosTilt

                        val px = c.x + r * x
                        val py = c.y - r * y
                        return Triple(px, py, z)
                    }

                    // 1. Latitudes (Equator at 0°, Tropics at ±36°)
                    val latitudes = listOf(-36f, 0f, 36f)
                    for (lat in latitudes) {
                        val frontPath = androidx.compose.ui.graphics.Path()
                        val backPath = androidx.compose.ui.graphics.Path()
                        var frontStarted = false
                        var backStarted = false

                        val steps = 36
                        for (s in 0..steps) {
                            val phi = (s.toDouble() / steps) * 2.0 * Math.PI
                            val (px, py, z) = project(lat, phi)
                            if (z >= -0.05f) {
                                if (!frontStarted) {
                                    frontPath.moveTo(px, py)
                                    frontStarted = true
                                } else {
                                    frontPath.lineTo(px, py)
                                }
                            } else {
                                frontStarted = false
                            }

                            if (z < 0.05f) {
                                if (!backStarted) {
                                    backPath.moveTo(px, py)
                                    backStarted = true
                                } else {
                                    backPath.lineTo(px, py)
                                }
                            } else {
                                backStarted = false
                            }
                        }

                        drawPath(
                            path = backPath,
                            color = tint.copy(alpha = 0.22f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW * 0.7f)
                        )
                        drawPath(
                            path = frontPath,
                            color = tint.copy(alpha = if (lat == 0f) 0.85f else 0.65f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (lat == 0f) strokeW else strokeW * 0.8f)
                        )
                    }

                    // 2. Longitudes (6 Meridians rotating smoothly with globeRotation)
                    val rotRad = Math.toRadians(globeRotation.value.toDouble())
                    val numMeridians = 6
                    for (m in 0 until numMeridians) {
                        val baseLon = rotRad + m * (2.0 * Math.PI / numMeridians)
                        val frontPath = androidx.compose.ui.graphics.Path()
                        val backPath = androidx.compose.ui.graphics.Path()
                        var frontStarted = false
                        var backStarted = false

                        val steps = 24
                        for (s in 0..steps) {
                            val lat = -90f + (180f * s / steps)
                            val (px, py, z) = project(lat, baseLon)
                            if (z >= -0.05f) {
                                if (!frontStarted) {
                                    frontPath.moveTo(px, py)
                                    frontStarted = true
                                } else {
                                    frontPath.lineTo(px, py)
                                }
                            } else {
                                frontStarted = false
                            }

                            if (z < 0.05f) {
                                if (!backStarted) {
                                    backPath.moveTo(px, py)
                                    backStarted = true
                                } else {
                                    backPath.lineTo(px, py)
                                }
                            } else {
                                backStarted = false
                            }
                        }

                        drawPath(
                            path = backPath,
                            color = tint.copy(alpha = 0.20f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW * 0.7f)
                        )
                        drawPath(
                            path = frontPath,
                            color = tint.copy(alpha = 0.85f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                        )
                    }
                }
            }
        }
        NavDestination.TIMER -> {
            Canvas(
                modifier = modifier
                    .size(22.dp)
                    .graphicsLayer {
                        rotationZ = hourglassFlip.value
                    }
            ) {
                val w = size.width
                val h = size.height
                val padX = 3.5.dp.toPx()
                val padY = 2.5.dp.toPx()
                val top = padY
                val bot = h - padY
                val left = padX
                val right = w - padX
                val midX = w / 2f
                val midY = h / 2f
                val neckHalf = 1.2.dp.toPx()
                val strokeW = 1.5.dp.toPx()

                // Top & bottom horizontal caps
                drawLine(
                    color = tint,
                    start = Offset(left, top),
                    end = Offset(right, top),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(left, bot),
                    end = Offset(right, bot),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )

                // Hourglass glass walls
                val glassPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left + 1.dp.toPx(), top)
                    lineTo(midX - neckHalf, midY)
                    lineTo(left + 1.dp.toPx(), bot)
                    lineTo(right - 1.dp.toPx(), bot)
                    lineTo(midX + neckHalf, midY)
                    lineTo(right - 1.dp.toPx(), top)
                    close()
                }
                drawPath(
                    path = glassPath,
                    color = tint,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                )

                // Inner cavity path for clean, silhouette-clipped sand fill
                val innerCavity = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left + strokeW, top + strokeW / 2f)
                    lineTo(midX - neckHalf, midY)
                    lineTo(left + strokeW, bot - strokeW / 2f)
                    lineTo(right - strokeW, bot - strokeW / 2f)
                    lineTo(midX + neckHalf, midY)
                    lineTo(right - strokeW, top + strokeW / 2f)
                    close()
                }

                clipPath(innerCavity) {
                    val maxFillHeight = (bot - midY) * 0.52f
                    val flow = sandProgress.value.coerceIn(0f, 1f)
                    val flip = hourglassFlip.value

                    // Chamber B (started at bottom when rotation=0; turns to visual top when rotation=180)
                    val remainingUpper = maxFillHeight * (1f - flow)
                    if (remainingUpper > 0.2.dp.toPx()) {
                        if (flip < 170f) {
                            // Upright resting state or during 180° flip: sand sits against bottom cap
                            drawRect(
                                color = tint,
                                topLeft = Offset(left, bot - maxFillHeight),
                                size = androidx.compose.ui.geometry.Size(right - left, maxFillHeight)
                            )
                        } else {
                            // Flipped upside down: sand funnels into neck midY and drains down
                            drawRect(
                                color = tint,
                                topLeft = Offset(left, midY),
                                size = androidx.compose.ui.geometry.Size(right - left, remainingUpper)
                            )
                        }
                    }

                    // Trickle stream passing through neck into lower chamber during downward flow
                    if (flow in 0.03f..0.97f && flip > 90f) {
                        val streamReach = (midY - top) * (0.35f + 0.35f * flow)
                        drawLine(
                            color = tint,
                            start = Offset(midX, midY),
                            end = Offset(midX, midY - streamReach),
                            strokeWidth = 1.3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Chamber A (started at top when rotation=0; turns to visual bottom when rotation=180)
                    // Fills up from local top (which is visual bottom when rotated 180°) towards midY
                    val currentLower = maxFillHeight * flow
                    if (currentLower > 0.2.dp.toPx()) {
                        drawRect(
                            color = tint,
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(right - left, currentLower)
                        )
                    }
                }
            }
        }
        NavDestination.STOPWATCH -> {
            Box(
                modifier = modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = destination.label,
                    tint = tint,
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(modifier = Modifier.size(12.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f + 1.dp.toPx())
                    val rad = Math.toRadians((needleAngle.value - 90.0))
                    val length = 4.2f.dp.toPx()
                    val end = Offset(
                        (center.x + length * kotlin.math.cos(rad)).toFloat(),
                        (center.y + length * kotlin.math.sin(rad)).toFloat()
                    )
                    drawLine(
                        color = tint,
                        start = center,
                        end = end,
                        strokeWidth = 1.5f.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = tint, radius = 1.2f.dp.toPx(), center = center)
                }
            }
        }
        NavDestination.BEDTIME -> {
            Box(
                modifier = modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bedtime,
                    contentDescription = destination.label,
                    tint = tint,
                    modifier = Modifier.fillMaxSize()
                )
                if (zProgress.value > 0f && zProgress.value < 1f) {
                    val p = zProgress.value
                    // Floating Z1
                    Text(
                        text = "z",
                        color = tint.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .offset(x = 6.dp + (4.dp * p), y = (-3).dp - (8.dp * p))
                    )
                    // Floating Z2
                    if (p > 0.2f) {
                        val p2 = ((p - 0.2f) / 0.8f).coerceIn(0f, 1f)
                        Text(
                            text = "Z",
                            color = tint.copy(alpha = (1f - p2).coerceIn(0f, 1f)),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .offset(x = 10.dp + (3.dp * p2), y = (-7).dp - (6.dp * p2))
                        )
                    }
                }
            }
        }
    }
}
