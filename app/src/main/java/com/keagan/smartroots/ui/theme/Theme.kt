package com.keagan.smartroots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3DDC84), // mint green
    secondary = Color(0xFF23A8E0), // aqua blue
    background = Color(0xFF0B0F0B),
    surface = Color(0xFF0F1510),
    surfaceVariant = Color(0xFF0B110B),
    onPrimary = Color(0xFF053116),
    onSecondary = Color(0xFFE7FFE7),
    onBackground = Color(0xFFE7FFE7),
    onSurface = Color(0xFFE7FFE7)
)

private val LightPastelColors = lightColorScheme(
    primary = Color(0xFF7BE3B3), // pastel mint
    secondary = Color(0xFF79C7FF), // pastel blue
    background = Color(0xFFF7FBF8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF4FAF7),
    onPrimary = Color(0xFF08361B),
    onSecondary = Color(0xFF16321F),
    onBackground = Color(0xFF16321F),
    onSurface = Color(0xFF16321F)
)

@Composable
fun SmartRootsTheme(light: Boolean, content: @Composable () -> Unit) {
    val colors = if (light) LightPastelColors else DarkColors
    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
