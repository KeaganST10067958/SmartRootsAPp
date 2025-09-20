package com.keagan.smartroots.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PillSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (checked) MaterialTheme.colorScheme.primary else
        MaterialTheme.colorScheme.surfaceVariant
    val fg = if (checked) MaterialTheme.colorScheme.onPrimary else
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = fg.copy(alpha = 0.15f),
            contentColor = fg,
            shape = CircleShape
        ) {
            Text(
                if (checked) "ON" else "OFF",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
