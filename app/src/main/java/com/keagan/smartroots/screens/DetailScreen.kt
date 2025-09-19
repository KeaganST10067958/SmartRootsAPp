package com.keagan.smartroots.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

/* -----------------------------------------------------------------------
   DETAIL SCREEN (self-contained)
   ----------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    metricKey: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(metricKey.replaceFirstChar { it.uppercase() }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { pads ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (metricKey) {
                "fan" -> FanPanel()
                "irrigation" -> IrrigationPanel()
                "mold" -> MoldPanel()
                "notes" -> NotesPanelFancy()
                "camera" -> CameraPanel(context)
                else -> SensorPanel(metricKey)
            }
        }
    }
}

/* ---------------------------- Sensor panel ----------------------------- */

private data class SensorSpec(
    val icon: ImageVector,
    val label: String,
    val unit: String,
    val min: Float,
    val max: Float,
    val ideal: ClosedFloatingPointRange<Float>? = null
)

@Composable
private fun SensorPanel(metricKey: String) {
    val spec = when (metricKey) {
        "humidity" -> SensorSpec(Icons.Rounded.WaterDrop, "Humidity", "%", 40f, 90f, 55f..75f)
        "temperature" -> SensorSpec(Icons.Rounded.Thermostat, "Temperature", "°C", 16f, 34f, 20f..28f)
        "ph" -> SensorSpec(Icons.Rounded.Science, "Soil pH", "pH", 5.2f, 7.2f, 5.8f..6.5f)
        "ec" -> SensorSpec(Icons.Rounded.Bolt, "Electrical Conductivity", "mS/cm", 0.2f, 2.5f, 0.8f..2.0f)
        "water" -> SensorSpec(Icons.Rounded.WaterDrop, "Water Level", "%", 20f, 100f, 40f..100f)
        else -> SensorSpec(Icons.Rounded.Info, metricKey, "", 0f, 1f, null)
    }

    var value by remember { mutableFloatStateOf((spec.min + spec.max) / 2f) }
    val series = remember { mutableStateListOf<Float>() }

    LaunchedEffect(metricKey) {
        series.clear()
        while (true) {
            value = Random.nextDouble(spec.min.toDouble(), spec.max.toDouble()).toFloat()
            series.add(value)
            if (series.size > 36) series.removeAt(0)
            delay(1500)
        }
    }

    val (statusText, statusColor) = when {
        spec.ideal == null -> "—" to MaterialTheme.colorScheme.outline
        value < spec.ideal.start -> "Low" to MaterialTheme.colorScheme.tertiary
        value > spec.ideal.endInclusive -> "High" to MaterialTheme.colorScheme.tertiary
        else -> "Ideal" to MaterialTheme.colorScheme.primary
    }

    ValueHeader(
        icon = spec.icon,
        label = spec.label,
        valueText = value.roundToInt().toString(),
        unit = spec.unit,
        statusChip = { StatusPill(statusText, statusColor) }
    )

    SRSectionCard(title = "Last 24 readings", subtitle = "Live feed (simulated)") {
        Sparkline(series)
        Spacer(Modifier.height(8.dp))
        Row {
            InfoStat("Min", "${series.minOrNull()?.prettyInt() ?: "--"} ${spec.unit}")
            InfoStat("Max", "${series.maxOrNull()?.prettyInt() ?: "--"} ${spec.unit}")
            spec.ideal?.let { ideal ->
                InfoStat("Ideal", "${ideal.start.roundToInt()}–${ideal.endInclusive.roundToInt()} ${spec.unit}")
            }
        }
    }
}

/* ------------------------------ Fan panel ------------------------------ */

@Composable
private fun FanPanel() {
    var fanOn by remember { mutableStateOf(false) }
    var boostMin by remember { mutableFloatStateOf(10f) }

    ValueHeader(
        icon = Icons.Rounded.Air,
        label = "Circulation Fan",
        valueText = if (fanOn) "ON" else "OFF",
        statusChip = {
            StatusPill(
                if (fanOn) "Running" else "Stopped",
                if (fanOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    )

    SRSectionCard("Manual control", "Quickly move air to dry the canopy.") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fan", modifier = Modifier.weight(1f))
            Switch(checked = fanOn, onCheckedChange = { fanOn = it })
        }
        Spacer(Modifier.height(8.dp))
        Text("Boost for ${boostMin.roundToInt()} min")
        Slider(
            value = boostMin,
            onValueChange = { boostMin = it },
            valueRange = 5f..30f,
            steps = 5
        )
        Button(onClick = { fanOn = true }) { Text("Start Boost") }
    }

    SRSectionCard("Tip") { Text("After every irrigation, run the fan 10 min to reduce mould risk.") }
}

/* --------------------------- Irrigation panel -------------------------- */

@Composable
private fun IrrigationPanel() {
    var auto by remember { mutableStateOf(true) }
    var seconds by remember { mutableFloatStateOf(15f) }
    var intervalH by remember { mutableFloatStateOf(2f) }

    ValueHeader(
        icon = Icons.Rounded.WaterDrop,
        label = "Irrigation",
        valueText = if (auto) "AUTO" else "MANUAL",
        statusChip = {
            StatusPill(
                if (auto) "Scheduled" else "Off",
                if (auto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    )

    SRSectionCard("Schedule", "Simple time-based priming for fodder mats.") {
        Text("Duration: ${seconds.roundToInt()} s")
        Slider(
            value = seconds,
            onValueChange = { seconds = it },
            valueRange = 5f..60f,
            steps = 10
        )
        Spacer(Modifier.height(4.dp))
        Text("Every: ${intervalH.roundToInt()} h (06:00–20:00)")
        Slider(
            value = intervalH,
            onValueChange = { intervalH = it },
            valueRange = 1f..6f,
            steps = 4
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto", modifier = Modifier.weight(1f))
            Switch(checked = auto, onCheckedChange = { auto = it })
        }
    }

    SRSectionCard("Manual run") {
        Button(onClick = { /* simulate pump run */ }) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Run pump ${seconds.roundToInt()} s")
        }
    }
}

/* ----------------------------- Mould panel ----------------------------- */

@Composable
private fun MoldPanel() {
    var temp by remember { mutableFloatStateOf(22f) }
    var rh by remember { mutableFloatStateOf(70f) }
    var minutes by remember { mutableFloatStateOf(45f) }
    var fanOn by remember { mutableStateOf(true) }

    val mri = computeMoldRiskIndex(temp, rh, minutes.roundToInt(), fanOn)
    val (label, color) = when {
        mri >= 67 -> "High" to MaterialTheme.colorScheme.error
        mri >= 34 -> "Caution" to MaterialTheme.colorScheme.tertiary
        else -> "OK" to MaterialTheme.colorScheme.primary
    }

    ValueHeader(
        icon = Icons.Rounded.Warning,
        label = "Mould Watch",
        valueText = mri.roundToInt().toString(),
        unit = "MRI",
        statusChip = { StatusPill(label, color) }
    )

    SRSectionCard("Current conditions") {
        Row {
            InfoStat("Temp", "${temp.roundToInt()} °C")
            InfoStat("Humidity", "${rh.roundToInt()} %")
            InfoStat("Since irrigation", "${minutes.roundToInt()} min")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fan", modifier = Modifier.weight(1f))
            Switch(checked = fanOn, onCheckedChange = { fanOn = it })
        }
    }

    SRSectionCard("Advice") { Text(adviceForMold(mri)) }

    SRSectionCard("Try adjustments") {
        Text("Temperature: ${temp.roundToInt()} °C")
        Slider(
            value = temp,
            onValueChange = { temp = it },
            valueRange = 15f..30f
        )
        Text("Humidity: ${rh.roundToInt()} %")
        Slider(
            value = rh,
            onValueChange = { rh = it },
            valueRange = 40f..95f
        )
        Text("Minutes since irrigation: ${minutes.roundToInt()}")
        Slider(
            value = minutes,
            onValueChange = { minutes = it },
            valueRange = 0f..240f,
            steps = 10
        )
    }
}

/* ------------------------------ Notes panel ---------------------------- */

@Composable
private fun NotesPanelFancy() {
    var text by remember { mutableStateOf("") }
    ValueHeader(
        icon = Icons.AutoMirrored.Rounded.Notes,
        label = "Notes",
        valueText = "Open"
    )
    SRSectionCard("Write a note") {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            placeholder = { Text("Type here…") }
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { /* save locally if you want */ }, modifier = Modifier.align(Alignment.End)) {
            Text("Save")
        }
    }
}

/* ------------------------------ Camera panel --------------------------- */

@Composable
private fun CameraPanel(context: Context) {
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> pickedUri = uri }

    ValueHeader(
        icon = Icons.Rounded.CameraAlt,
        label = "Camera",
        valueText = "Open"
    )

    SRSectionCard("Gallery") {
        Button(onClick = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Pick a photo") }

        AnimatedVisibility(visible = pickedUri != null) {
            val bmp = remember(pickedUri) { pickedUri?.let { loadBitmap(context, it) } }
            bmp?.let {
                Spacer(Modifier.height(12.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
                        .padding(6.dp)
                )
            }
        }
    }
}

/* ------------------------- Helpers & MRI logic ------------------------- */

private fun loadBitmap(context: Context, uri: Uri): Bitmap {
    val src = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(src)
}

private fun computeMoldRiskIndex(
    tempC: Float,
    rhPct: Float,
    minutesSinceIrrigation: Int,
    fanOn: Boolean
): Float {
    val rhScore = ((rhPct - 50f) / 40f).coerceIn(0f, 1f) * 50f         // 0..50
    val tScore = ((tempC - 18f) / 10f).coerceIn(0f, 1f) * 30f          // 0..30
    val wetScore = (1f - (minutesSinceIrrigation / 120f).coerceIn(0f, 1f)) * 20f // 0..20
    val fanBonus = if (fanOn) -10f else 0f
    return (rhScore + tScore + wetScore + fanBonus).coerceIn(0f, 100f)
}

private fun adviceForMold(mri: Float): String = when {
    mri >= 67 -> "Action needed: Increase airflow, shorten watering, and dry mats. Clean trays with dilute H₂O₂."
    mri >= 34 -> "Watch: Add a 10-min fan boost after irrigation and avoid over-watering."
    else -> "Good: Keep current schedule. Ensure gentle airflow across the trays."
}

private fun Float.prettyInt(): String = this.roundToInt().toString()

/* -----------------------------------------------------------------------
   Embedded UI kit (local/private so there’s no cross-file confusion)
   ----------------------------------------------------------------------- */

@Composable
private fun SRSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ValueHeader(
    icon: ImageVector,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,          // keep first optional to satisfy lint
    unit: String = "",
    statusChip: @Composable (() -> Unit)? = null,
    gradient: Brush? = null
) {
    val resolvedGradient = gradient ?: Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(0.20f),
            MaterialTheme.colorScheme.primary.copy(0.05f)
        )
    )

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.background(resolvedGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            valueText,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (unit.isNotBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                unit,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                statusChip?.invoke()
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.18f), contentColor = color, shape = CircleShape) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val stroke = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val w = size.width
        val h = size.height

        for (i in 1..3) {
            val y = h * i / 4f
            drawLine(grid, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
        }

        if (values.isEmpty()) return@Canvas
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 1f
        val stepX = if (values.size <= 1) w else w / (values.size - 1).toFloat()

        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = if (max == min) h / 2f else h - (v - min) / (max - min) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = stroke, style = Stroke(width = 6f))
    }
}

@Composable
private fun InfoStat(title: String, value: String) {
    Column(Modifier.padding(end = 16.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
