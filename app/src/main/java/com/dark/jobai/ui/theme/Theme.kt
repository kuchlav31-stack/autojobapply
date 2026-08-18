package com.dark.jobai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============ DARK COLOR SCHEME ============
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.Black,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = TextWhite,
    secondary = InfoBlue,
    onSecondary = TextWhite,
    tertiary = WarningOrange,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = TextWhite,
    outline = BorderGray,
    outlineVariant = BorderGray.copy(alpha = 0.5f)
)

// ============ LIGHT COLOR SCHEME ============
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenLight.copy(alpha = 0.2f),
    onPrimaryContainer = TextBlack,
    secondary = InfoBlue,
    onSecondary = Color.White,
    tertiary = WarningOrange,
    background = BackgroundLight,
    onBackground = TextBlack,
    surface = SurfaceLightMode,
    onSurface = TextBlack,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextGrayLight,
    error = ErrorRed,
    onError = Color.White,
    outline = BorderGrayLight,
    outlineVariant = BorderGrayLight.copy(alpha = 0.5f)
)

@Composable
fun JobAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JobAITypography,
        shapes = JobAIShapes,
        content = content
    )
}