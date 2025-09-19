package com.keagan.smartroots.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.model.Metric
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MetricTile(metric: Metric, onClick: () -> Unit) {
    val special = setOf("mold", "fan", "irrigation", "notes", "camera")

    if (metric.key in special) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeadingIcon(metric.icon)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(metric.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(metric.tipRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
        return
    }

    var target by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(metric.key) {
        while (true) {
            if (metric.min < metric.max) {
                target = Random.nextDouble(metric.min.toDouble(), metric.max.toDouble()).toFloat()
            }
            kotlinx.coroutines.delay(1800)
        }
    }

    val statusLabel = run {
        val v = target
        when {
            v == null || metric.ideal == null -> "—"
            v < metric.ideal.start -> "Low"
            v > metric.ideal.endInclusive -> "High"
            else -> "Ideal"
        }
    }

    val scheme = MaterialTheme.colorScheme
    val statusColor = when (statusLabel) {
        "Low", "High" -> scheme.tertiary
        "Ideal" -> scheme.primary
        else -> scheme.outline
    }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "pulseAnim"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.fillMaxSize().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp)
                    .background(scheme.primary.copy(alpha = pulse), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) { Icon(metric.icon, null, tint = scheme.primary) }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(metric.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(metric.tipRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (metric.min < metric.max) {
                    val v = target ?: metric.min
                    val pct = ((v - metric.min) / (metric.max - metric.min)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                            .background(scheme.surfaceVariant, MaterialTheme.shapes.small),
                        trackColor = scheme.surfaceVariant
                    )
                }
                Box(
                    modifier = Modifier.background(statusColor.copy(alpha = 0.12f), MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = statusLabel, style = MaterialTheme.typography.labelMedium, color = statusColor)
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = if (metric.unit.isEmpty() || target == null) "—" else "${target!!.roundToInt()} ${metric.unit}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (statusLabel == "Ideal") scheme.onSurface else scheme.tertiary
            )
        }
    }
}

@Composable
private fun LeadingIcon(icon: ImageVector) {
    Box(
        Modifier.size(56.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.large),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
}
