package com.keagan.smartroots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---------------- Dark (new leaf palette) ----------------
private val DarkColors = darkColorScheme(
    primary = LeafGreen,            // buttons, active chips
    onPrimary = OnDarkHigh,         // text/icons on primary

    secondary = LeafChartreuse,     // accents (status, small highlights)
    onSecondary = LeafNight,

    tertiary = LeafForest,          // "warning-ish" accent we use for Low/High state
    onTertiary = OnDarkHigh,

    background = LeafNight,         // app background
    onBackground = OnDarkHigh,

    surface = Color(0xFF0A1A0A),    // slightly lighter than background
    onSurface = OnDarkHigh,

    surfaceVariant = Color(0xFF0E2010),
    onSurfaceVariant = OnDarkMed,

    outline = OutlineDark
    // error / errorContainer left default from M3
)

// ---------------- Light (unchanged from your current look) ----------------
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
