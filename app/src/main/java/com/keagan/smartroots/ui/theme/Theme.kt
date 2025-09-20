package com.keagan.smartroots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SO_Primary_L,   onPrimary = Color.White,
    secondary = SO_Secondary_L, onSecondary = Color.White,
    tertiary = SO_Tertiary_L, onTertiary = Color.White,
    background = SO_Bg_L, onBackground = SO_OnSurf_L,
    surface = SO_Surf_L, onSurface = SO_OnSurf_L,
    surfaceVariant = SO_SurfVar_L, onSurfaceVariant = SO_OnSurf_L.copy(alpha = 0.80f),
    outline = SO_OnSurf_L.copy(alpha = 0.20f)
)

private val DarkColors = darkColorScheme(
    primary = SO_Primary_D,   onPrimary = Color(0xFF0C130F),
    secondary = SO_Secondary_D, onSecondary = Color(0xFF120C08),
    tertiary = SO_Tertiary_D, onTertiary = Color(0xFF0E0C16),
    background = SO_Bg_D, onBackground = SO_OnSurf_D,
    surface = SO_Surf_D, onSurface = SO_OnSurf_D,
    surfaceVariant = SO_SurfVar_D, onSurfaceVariant = SO_OnSurf_D.copy(alpha = 0.85f),
    outline = SO_OnSurf_D.copy(alpha = 0.24f)
)

@Composable
fun SmartRootsTheme(light: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (light) LightColors else DarkColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
