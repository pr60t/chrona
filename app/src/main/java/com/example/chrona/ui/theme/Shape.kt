package com.example.chrona.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ChronaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp), // Canonical Chrona Card Radius
    extraLarge = RoundedCornerShape(28.dp) // Bottom Sheet Radius
)

val CardCornerShape = RoundedCornerShape(20.dp)
val PillCornerShape = RoundedCornerShape(100.dp)
val ImageThumbShape = RoundedCornerShape(12.dp)
val SheetCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
