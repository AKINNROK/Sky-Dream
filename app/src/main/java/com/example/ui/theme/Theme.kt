package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MysticViolet,
    onPrimary = DeepObsidian,
    primaryContainer = VelvetPurple,
    onPrimaryContainer = TextPrimary,
    secondary = RoseCrimson,
    onSecondary = DeepObsidian,
    tertiary = CyanNeon,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

private val LightColorScheme = DarkColorScheme // Default to atmospheric dark mode for immersive novel feel

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Immersive story mode default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
