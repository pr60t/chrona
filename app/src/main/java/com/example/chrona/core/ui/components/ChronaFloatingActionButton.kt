package com.example.chrona.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared design tokens for the primary Floating Action Button used across Chrona screens.
 */
object ChronaFabDefaults {
    val size: Dp = 84.dp
    val iconSize: Dp = 36.dp
    val cornerRadius: Dp = 26.dp
    val elevation: Dp = 3.dp
    val endPadding: Dp = 20.dp
    val bottomPadding: Dp = 20.dp
}

/**
 * Standardized Floating Action Button that guarantees identical dimensions, corner radius,
 * icon sizing, elevation, and colors across all Chrona screens (Alarms, World Clock, etc.).
 */
@Composable
fun ChronaFloatingActionButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color(0xFF0B1320),
        shape = RoundedCornerShape(ChronaFabDefaults.cornerRadius),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = ChronaFabDefaults.elevation),
        modifier = modifier.size(ChronaFabDefaults.size)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(ChronaFabDefaults.iconSize)
        )
    }
}
