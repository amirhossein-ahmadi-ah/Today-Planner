package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Color Scheme
private val CreamColorScheme = lightColorScheme(
    primary = SagePrimary,
    secondary = TealAccent,
    background = CreamBg,
    surface = CreamSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = Color(0xFFEFE8DE),
    onSurfaceVariant = Color(0xFF6C5750)
)

// Cosmic Dark Color Scheme
private val CosmicColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    background = CosmicBg,
    surface = CosmicSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CosmicText,
    onSurface = CosmicText,
    surfaceVariant = Color(0xFF1B1E34),
    onSurfaceVariant = Color(0xFF9EAAD1)
)

// Amoled Color Scheme
private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    secondary = AmoledAccent,
    background = AmoledBg,
    surface = AmoledSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AmoledText,
    onSurface = AmoledText,
    surfaceVariant = Color(0xFF16161C),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun SmartPlannerTheme(
    themePreset: String = "dark", // "light", "dark", "amoled"
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreset) {
        "light" -> CreamColorScheme
        "amoled" -> AmoledColorScheme
        else -> CosmicColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
