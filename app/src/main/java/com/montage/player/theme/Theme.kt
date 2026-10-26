package com.montage.player.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpotifyDarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    secondary = SpotifyGreenHover,
    onSecondary = Color.Black,
    tertiary = SpotifyElevated,
    background = SpotifyBackground,
    onBackground = SpotifyTextPrimary,
    surface = SpotifySurface,
    onSurface = SpotifyTextPrimary,
    surfaceVariant = SpotifyElevated,
    onSurfaceVariant = SpotifyTextSecondary
)

@Composable
fun MontagePlayerTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SpotifyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
