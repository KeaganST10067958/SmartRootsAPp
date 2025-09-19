package com.keagan.smartroots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Night: forest green
private val DarkForestColors = darkColorScheme(
    primary = Color(0xFF2EA36A),     // forest mint
    secondary = Color(0xFF3FB489),
    background = Color(0xFF0A100C),
    surface = Color(0xFF0E1511),
    surfaceVariant = Color(0xFF142019),
    onPrimary = Color(0xFF021C10),
    onSecondary = Color(0xFFE7FFE7),
    onBackground = Color(0xFFE7FFE7),
    onSurface = Color(0xFFE7FFE7)
)

// Day: high-contrast for sunlight
private val LightSunColors = lightColorScheme(
    primary = Color(0xFF1E7A4F),     // deep green (good outdoors)
    secondary = Color(0xFF0B6E99),   // teal/blue accents
    background = Color(0xFFFAFAFA),  // near white for glare resistance
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F4F2),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A0F0C),// near-black text
    onSurface = Color(0xFF0A0F0C)
)

@Composable
fun SmartRootsTheme(light: Boolean, content: @Composable () -> Unit) {
    val colors = if (light) LightSunColors else DarkForestColors
    MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
}
