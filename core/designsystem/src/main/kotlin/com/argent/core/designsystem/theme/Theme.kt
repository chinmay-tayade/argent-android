package com.argent.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ArgentIndigo,
    onPrimary = ArgentSurfaceLight,
    surface = ArgentSurfaceLight,
    onSurface = ArgentInkLight,
)

private val DarkColors = darkColorScheme(
    primary = ArgentIndigoDark,
    onPrimary = ArgentInkLight,
    surface = ArgentSurfaceDark,
    onSurface = ArgentInkDark,
)

@Composable
fun ArgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ArgentTypography,
        content = content,
    )
}
