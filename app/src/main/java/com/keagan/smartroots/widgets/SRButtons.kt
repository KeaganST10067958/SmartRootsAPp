package com.keagan.smartroots.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Palette (you can tweak these)
val SproutGreen  = Color(0xFF28B487) // main veg
val CarrotOrange = Color(0xFFF39C12) // fodder
val Indigo       = Color(0xFF3F51B5) // harvest/utility
val Teal         = Color(0xFF009688) // optional alt

@Composable
fun SRFilledButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor   = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}
