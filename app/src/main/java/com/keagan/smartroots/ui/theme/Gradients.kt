package com.keagan.smartroots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

/** Teal→Orange header gradient; great for ValueHeader backgrounds. */
@Composable
fun srHeroGradient(): Brush {
    val c = MaterialTheme.colorScheme
    return Brush.linearGradient(
        listOf(
            c.primary.copy(alpha = 0.22f),
            c.secondary.copy(alpha = 0.18f)
        )
    )
}

/** Subtle card sheen: primary tint fading to surface. */
@Composable
fun srCardSheen(): Brush {
    val c = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        0.0f to c.primary.copy(alpha = 0.10f),
        1.0f to c.surface.copy(alpha = 0.00f)
    )
}
