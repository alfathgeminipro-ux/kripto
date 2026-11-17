package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonexDarkColorScheme = darkColorScheme(
    primary = MonexNeonLime,
    onPrimary = MonexOnNeon,
    primaryContainer = MonexNeonLimeSoft,
    onPrimaryContainer = MonexOnNeon,
    secondary = MonexBlueOrb,
    onSecondary = MonexTextPrimary,
    tertiary = MonexPurpleOrb,
    onTertiary = MonexTextPrimary,
    error = MonexPink,
    onError = MonexTextPrimary,
    background = MonexBg,
    onBackground = MonexTextPrimary,
    surface = MonexSurface,
    onSurface = MonexTextPrimary,
    surfaceVariant = MonexSurfaceElevated,
    onSurfaceVariant = MonexTextSecondary,
    outline = MonexGlassBorder
)

@Composable
fun MonexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonexDarkColorScheme,
        typography = MonexTypography,
        content = content
    )
}
