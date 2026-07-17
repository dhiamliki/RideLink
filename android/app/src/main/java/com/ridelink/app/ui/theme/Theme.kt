package com.ridelink.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoTint,
    onPrimaryContainer = IndigoInk,
    secondary = IndigoPressed,
    onSecondary = Color.White,
    secondaryContainer = IndigoTint,
    onSecondaryContainer = IndigoInk,
    background = Background,
    onBackground = TextStrong,
    surface = Surface,
    onSurface = TextStrong,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    outlineVariant = OutlineSoft,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerTint,
    onErrorContainer = DangerInk,
)

private val DarkColors = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoTintDark,
    onPrimaryContainer = IndigoTintOnDark,
    secondary = IndigoTintOnDark,
    onSecondary = IndigoTintDark,
    secondaryContainer = IndigoTintDark,
    onSecondaryContainer = IndigoTintOnDark,
    background = BackgroundDark,
    onBackground = TextStrongDark,
    surface = SurfaceDark,
    onSurface = TextStrongDark,
    surfaceVariant = SurfaceMutedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerTint,
    onErrorContainer = DangerInk,
)

@Composable
fun RideLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
