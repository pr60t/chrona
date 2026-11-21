package com.example.chrona.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Immutable
data class ChronaExtendedColors(
    val cardBackground: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val outline: Color,
    val divider: Color,
    val mapLand: Color,
    val mapStroke: Color,
    val success: Color,
    val warning: Color,
    val error: Color
)

val LocalChronaExtendedColors = staticCompositionLocalOf {
    ChronaDarkExtendedColors
}

val LocalIsDarkTheme = staticCompositionLocalOf {
    true
}

val ChronaDarkExtendedColors = ChronaExtendedColors(
    cardBackground = ChronaDarkCard,
    surfaceLowest = ChronaDarkLowest,
    surfaceLow = ChronaDarkLow,
    surfaceHigh = ChronaDarkHigh,
    surfaceHighest = ChronaDarkHighest,
    textPrimary = ChronaDarkTextPri,
    textSecondary = ChronaDarkTextSec,
    textMuted = ChronaDarkTextMuted,
    outline = ChronaDarkOutline,
    divider = ChronaDarkDivider,
    mapLand = ChronaDarkMapLand,
    mapStroke = ChronaDarkMapStroke,
    success = ChronaDarkSuccess,
    warning = ChronaDarkWarning,
    error = ChronaDarkError
)

val ChronaLightExtendedColors = ChronaExtendedColors(
    cardBackground = ChronaLightCard,
    surfaceLowest = ChronaLightLowest,
    surfaceLow = ChronaLightLow,
    surfaceHigh = ChronaLightHigh,
    surfaceHighest = ChronaLightHighest,
    textPrimary = ChronaLightTextPri,
    textSecondary = ChronaLightTextSec,
    textMuted = ChronaLightTextMuted,
    outline = ChronaLightOutline,
    divider = ChronaLightDivider,
    mapLand = ChronaLightMapLand,
    mapStroke = ChronaLightMapStroke,
    success = ChronaLightSuccess,
    warning = ChronaLightWarning,
    error = ChronaLightError
)

private val DarkColorScheme = darkColorScheme(
    primary = ChronaDarkAccent,
    onPrimary = ChronaDarkOnAccent,
    primaryContainer = ChronaDarkAccentCont,
    onPrimaryContainer = ChronaDarkOnAccentCont,
    secondary = ChronaDarkAccent,
    onSecondary = ChronaDarkOnAccent,
    background = ChronaDarkBg,
    onBackground = ChronaDarkTextPri,
    surface = ChronaDarkCard,
    onSurface = ChronaDarkTextPri,
    surfaceVariant = ChronaDarkHigh,
    onSurfaceVariant = ChronaDarkTextSec,
    outline = ChronaDarkOutline,
    outlineVariant = ChronaDarkDivider,
    error = ChronaDarkError,
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = ChronaLightAccent,
    onPrimary = ChronaLightOnAccent,
    primaryContainer = ChronaLightAccentCont,
    onPrimaryContainer = ChronaLightOnAccentCont,
    secondary = ChronaLightAccent,
    onSecondary = ChronaLightOnAccent,
    background = ChronaLightBg,
    onBackground = ChronaLightTextPri,
    surface = ChronaLightCard,
    onSurface = ChronaLightTextPri,
    surfaceVariant = ChronaLightHigh,
    onSurfaceVariant = ChronaLightTextSec,
    outline = ChronaLightOutline,
    outlineVariant = ChronaLightDivider,
    error = ChronaLightError,
    onError = Color(0xFFFFFFFF)
)

object ChronaThemeTokens {
    val extended: ChronaExtendedColors
        @Composable
        get() = LocalChronaExtendedColors.current

    val isDark: Boolean
        @Composable
        get() = LocalIsDarkTheme.current
}

@Composable
fun ChronaTheme(
    darkTheme: Boolean = true, // Default to true pitch-black AMOLED
    customAccent: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = if (customAccent != null) {
        if (darkTheme) {
            baseScheme.copy(
                primary = customAccent,
                onPrimary = if (customAccent.luminance() > 0.5f) Color(0xFF1B1B1D) else Color.White,
                secondary = customAccent,
                onSecondary = if (customAccent.luminance() > 0.5f) Color(0xFF1B1B1D) else Color.White,
                primaryContainer = customAccent.copy(alpha = 0.25f),
                onPrimaryContainer = customAccent
            )
        } else {
            val lightPrimary = if (customAccent.luminance() > 0.6f) Color(0xFF4C618A) else customAccent
            baseScheme.copy(
                primary = lightPrimary,
                onPrimary = Color.White,
                secondary = lightPrimary,
                onSecondary = Color.White,
                primaryContainer = customAccent.copy(alpha = 0.45f),
                onPrimaryContainer = Color(0xFF152238)
            )
        }
    } else {
        baseScheme
    }

    val extendedColors = if (darkTheme) ChronaDarkExtendedColors else ChronaLightExtendedColors

    CompositionLocalProvider(
        LocalChronaExtendedColors provides extendedColors,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ChronaShapes,
            content = content
        )
    }
}
