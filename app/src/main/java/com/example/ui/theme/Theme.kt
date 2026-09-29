package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SalimDarkColorScheme = darkColorScheme(
    primary = SalimBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = SalimOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF431407),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = SalimGreen,
    onTertiary = Color.White,
    background = SalimDarkBackground,
    onBackground = SalimDarkTextPrimary,
    surface = SalimDarkSurface,
    onSurface = SalimDarkTextPrimary,
    surfaceVariant = SalimDarkSurfaceElevated,
    onSurfaceVariant = SalimDarkTextSecondary,
    outline = SalimDarkSurfaceBorder,
    outlineVariant = Color(0xFF1C222E),
    error = SalimRed,
    onError = Color.White
)

private val SalimLightColorScheme = lightColorScheme(
    primary = SalimBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = SalimOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF431407),
    tertiary = SalimGreen,
    onTertiary = Color.White,
    background = SalimLightBackground,
    onBackground = SalimLightTextPrimary,
    surface = SalimLightSurface,
    onSurface = SalimLightTextPrimary,
    surfaceVariant = SalimLightSurfaceElevated,
    onSurfaceVariant = SalimLightTextSecondary,
    outline = SalimLightSurfaceBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = SalimRed,
    onError = Color.White
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SalimDarkColorScheme else SalimLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SalimTypography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SalimTheme(darkTheme = darkTheme, content = content)
}
