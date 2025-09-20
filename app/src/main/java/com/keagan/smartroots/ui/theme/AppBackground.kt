package com.keagan.smartroots.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.keagan.smartroots.R

@Composable
fun AppBackdrop(
    lightMode: Boolean,
    @DrawableRes lightRes: Int = R.drawable.bg_field_light,
    @DrawableRes darkRes: Int = R.drawable.bg_field_dark,
    content: @Composable () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(if (lightMode) lightRes else darkRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Stronger only in LIGHT mode to keep text/components readable on bright photos.
        if (lightMode) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to c.background.copy(alpha = 0.14f),
                            0.35f to c.background.copy(alpha = 0.22f),
                            1.00f to c.background.copy(alpha = 0.32f)
                        )
                    )
            )
        } else {
            // Subtle scrim in dark (contrast is already high)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to c.background.copy(alpha = 0.08f),
                            0.35f to c.background.copy(alpha = 0.12f),
                            1.00f to c.background.copy(alpha = 0.18f)
                        )
                    )
            )
        }

        content()
    }
}
