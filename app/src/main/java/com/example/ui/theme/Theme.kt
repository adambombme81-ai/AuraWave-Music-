package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraWaveColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Obsidian,
    primaryContainer = Color(0xFF003840),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = NeonPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0025),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = NeonViolet,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF33005A),
    onTertiaryContainer = Color(0xFFEADBFF),
    background = Obsidian,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF262244)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve custom neon audio aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraWaveColorScheme,
        typography = Typography,
        content = content
    )
}
