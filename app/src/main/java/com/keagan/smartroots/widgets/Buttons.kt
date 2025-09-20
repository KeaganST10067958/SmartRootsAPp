package com.keagan.smartroots.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SRButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val c = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary.copy(alpha = 0.90f),
            contentColor = c.onPrimary
        )
    ) { Text(text) }
}

@Composable
fun SRSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val c = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.secondary.copy(alpha = 0.90f),
            contentColor = c.onSecondary
        )
    ) { Text(text) }
}

@Composable
fun SRSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val c = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.tertiary.copy(alpha = 0.92f),
            contentColor = c.onTertiary
        )
    ) { Text(text) }
}

@Composable
fun SROutlinedSoft(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val c = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = c.onSurface
        )
    ) { Text(text) }
}
