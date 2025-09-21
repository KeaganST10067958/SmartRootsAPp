package com.keagan.smartroots.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.model.Metric
import kotlin.math.roundToInt
import kotlin.random.Random

private val DangerRed         = Color(0xFFD32F2F)
private val WarningOrange     = Color(0xFFFFA000)
private val SuccessLightGreen = Color(0xFF66BB6A)

private fun colorForStatus(status: String, scheme: ColorScheme): Color = when (status) {
    "High"  -> DangerRed
    "Low"   -> WarningOrange
    "Ideal" -> SuccessLightGreen
    else    -> scheme.onSurfaceVariant
}

@Composable
fun MetricTile(metric: Metric, onClick: () -> Unit) {
    val special = setOf("mold", "fan", "irrigation", "notes", "camera", "light")

    // Special tiles (navigate)
    if (metric.key in special) {
        var lightOn by remember { mutableStateOf(false) }

        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeadingIcon(metric.icon)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(metric.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(metric.tipRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                if (metric.key == "light") {
                    // Inline on/off control (doesn't navigate)
                    Spacer(Modifier.width(8.dp))
                    PillSwitch(
                        checked = lightOn,
                        onCheckedChange = { lightOn = it }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
            }
        }
        return
    }

    // Common tiles with simulated reading
    var reading by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(metric.key) {
        while (true) {
            if (metric.min < metric.max) {
                reading = Random
                    .nextDouble(metric.min.toDouble(), metric.max.toDouble())
                    .toFloat()
            }
            kotlinx.coroutines.delay(1800)
        }
    }

    val statusLabel = run {
        val v = reading
        when {
            v == null || metric.ideal == null -> "—"
            v < metric.ideal.start -> "Low"
            v > metric.ideal.endInclusive -> "High"
            else -> "Ideal"
        }
    }
    val scheme = MaterialTheme.colorScheme
    val statusColor = colorForStatus(statusLabel, scheme)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 168.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeadingIcon(metric.icon)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(metric.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        contentColor = statusColor,
                        shape = CircleShape
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Text(
                    text = stringResource(metric.tipRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface.copy(alpha = 0.70f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (metric.unit.isEmpty() || reading == null) "—"
                else "${reading!!.roundToInt()} ${metric.unit}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }
    }
}

@Composable
private fun LeadingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                MaterialTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}
