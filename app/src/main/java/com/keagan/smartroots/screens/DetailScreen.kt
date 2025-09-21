package com.keagan.smartroots.screens

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.compose.AsyncImage
import com.keagan.smartroots.components.PillSwitch
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.model.Note
import com.keagan.smartroots.ui.SRScaffold
import com.keagan.smartroots.ui.theme.srCardSheen
import com.keagan.smartroots.ui.theme.srHeroGradient
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.random.Random

private val DangerRed = Color(0xFFD32F2F)
private val WarningOrange = Color(0xFFFFA000)
private val SuccessLightGreen = Color(0xFF66BB6A)

private fun colorForStatus(label: String, scheme: ColorScheme): Color = when (label) {
    "High" -> DangerRed
    "Low" -> WarningOrange
    "Ideal" -> SuccessLightGreen
    else -> scheme.onSurfaceVariant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    metricKey: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    SRScaffold(
        topBar = {
            TopAppBar(
                title = { Text(metricKey.replaceFirstChar { it.uppercase() }) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
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
                "light" -> LightPanel()
                "irrigation" -> IrrigationPanel()
                "mold" -> MoldPanel()
                "notes" -> NotesPanelFancy()
                "camera" -> CameraPanel(context)
                else -> SensorPanel(metricKey)
            }
        }
    }
}

/* ---------------------------- Reusable UI ------------------------------ */

@Composable
private fun BigTile(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Box(Modifier.background(srCardSheen())) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
private fun TipsCard(title: String = "Tips", lines: List<String>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            lines.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
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
            kotlinx.coroutines.delay(1500)
        }
    }

    val (statusText, statusColor) = run {
        val label = when {
            spec.ideal == null -> "—"
            value < spec.ideal.start -> "Low"
            value > spec.ideal.endInclusive -> "High"
            else -> "Ideal"
        }
        label to colorForStatus(label, MaterialTheme.colorScheme)
    }

    ValueHeader(
        icon = spec.icon,
        label = spec.label,
        valueText = value.roundToInt().toString(),
        unit = spec.unit,
        valueColor = statusColor,
        statusChip = { StatusPill(statusText, statusColor) },
        gradient = srHeroGradient()
    )

    SRSectionCard(title = "Last 24 readings", subtitle = "Live feed (simulated)") {
        Sparkline(series)
        Spacer(Modifier.height(8.dp))
        Row {
            InfoStat("Min", "${series.minOrNull()?.roundToInt() ?: "--"} ${spec.unit}")
            InfoStat("Max", "${series.maxOrNull()?.roundToInt() ?: "--"} ${spec.unit}")
            spec.ideal?.let { ideal ->
                InfoStat("Ideal", "${ideal.start.roundToInt()}–${ideal.endInclusive.roundToInt()} ${spec.unit}")
            }
        }
    }

    if (metricKey == "humidity" || metricKey == "temperature") {
        Spacer(Modifier.height(12.dp))
        var fanOn by remember { mutableStateOf(false) }
        BigTile(title = "Fan", subtitle = "This controls the tent fan") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PillSwitch(checked = fanOn, onCheckedChange = { fanOn = it })
            }
        }
    }

    val tips = when (metricKey) {
        "humidity" -> listOf(
            if (statusText == "High") "Humidity high: increase airflow; shorten pump duration."
            else if (statusText == "Low") "Humidity low: consider slightly longer/closer pump cycles."
            else "Humidity within ideal range.",
            "Keep gentle airflow across canopy; avoid pooling water."
        )
        "temperature" -> listOf(
            if (statusText == "High") "Temp high: increase ventilation; consider shade/cooler intake."
            else if (statusText == "Low") "Temp low: reduce drafts; insulate base of tent/trays."
            else "Temperature within ideal range.",
            "Check pump timing; warmer rooms evaporate faster."
        )
        "ph" -> listOf(
            "If pH < 5.8 (acidic): add pH-Up in small increments; re-measure.",
            "If pH > 6.5 (alkaline): add pH-Down; re-measure.",
            "Stable pH keeps nutrients available to roots."
        )
        "ec" -> listOf(
            "Low EC: increase nutrient concentration gradually; avoid big jumps.",
            "High EC: dilute reservoir with clean water, then re-check."
        )
        else -> emptyList()
    }
    if (tips.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        TipsCard(lines = tips)
    }
}

/* ------------------------------ Fan panel ------------------------------ */

@Composable
private fun FanPanel() {
    var fanOn by remember { mutableStateOf(false) }
    ValueHeader(
        icon = Icons.Rounded.Air,
        label = "Circulation Fan",
        valueText = if (fanOn) "ON" else "OFF",
        valueColor = if (fanOn) SuccessLightGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        gradient = srHeroGradient()
    )
    BigTile(title = "Fan", subtitle = "This controls the tent fan") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillSwitch(checked = fanOn, onCheckedChange = { fanOn = it })
        }
    }
    Spacer(Modifier.height(12.dp))
    TipsCard(lines = listOf(
        if (fanOn) "Fan is ON; airflow reduces mould risk."
        else "Turn fan ON after irrigation or when humidity is high.",
        "Aim airflow across the canopy, not directly at seedlings."
    ))
}

@Composable
private fun LightPanel() {
    var lightOn by remember { mutableStateOf(false) }

    ValueHeader(
        icon = Icons.Rounded.LightMode,
        label = "Light",
        valueText = if (lightOn) "ON" else "OFF",
        valueColor = if (lightOn) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurfaceVariant,
        gradient = srHeroGradient()
    )

    BigTile(title = "Grow Lights", subtitle = "This controls the tent lights") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillSwitch(checked = lightOn, onCheckedChange = { lightOn = it })
        }
    }

    Spacer(Modifier.height(12.dp))
    TipsCard(lines = listOf(
        "Use longer photoperiods for leafy veg; shorter for fodder mats.",
        "If temperature rises too high, reduce light period or increase airflow."
    ))
}

/* --------------------------- Pump (Irrigation) ------------------------- */

@Composable
private fun IrrigationPanel() {
    var pumpOn by remember { mutableStateOf(false) }

    ValueHeader(
        icon = Icons.Rounded.WaterDrop,
        label = "Pump",
        valueText = if (pumpOn) "ON" else "OFF",
        valueColor = if (pumpOn) SuccessLightGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        gradient = srHeroGradient()
    )

    BigTile(title = "Pump", subtitle = "Toggle to run pump (simulated)") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillSwitch(checked = pumpOn, onCheckedChange = { pumpOn = it })
        }
    }

    Spacer(Modifier.height(12.dp))
    SRSectionCard("Schedule (info)") {
        Text("Duration: 15 s")
        Text("Every: 2 h (active 06:00–20:00)")
    }
    Spacer(Modifier.height(12.dp))
    TipsCard(lines = listOf(
        "Short, frequent cycles keep mats moist without pooling.",
        "Increase interval if you see mould or standing water.",
        "Decrease interval if edges dry out between cycles."
    ))
}

/* ----------------------------- Mould panel ----------------------------- */

@Composable
private fun MoldPanel() {
    var temp by remember { mutableStateOf(22f) }
    var rh by remember { mutableStateOf(70f) }
    var minutes by remember { mutableStateOf(45f) }
    var fanOn by remember { mutableStateOf(true) }

    val mri = computeMoldRiskIndex(temp, rh, minutes.roundToInt(), fanOn)
    val (label, color) = when {
        mri >= 67 -> "High" to DangerRed
        mri >= 34 -> "Caution" to WarningOrange
        else -> "OK" to SuccessLightGreen
    }

    ValueHeader(
        icon = Icons.Rounded.Warning,
        label = "Mould Watch",
        valueText = mri.roundToInt().toString(),
        unit = "MRI",
        valueColor = color,
        statusChip = { StatusPill(label, color) },
        gradient = srHeroGradient()
    )

    BigTile(title = "Fan", subtitle = "This controls the tent fan") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillSwitch(checked = fanOn, onCheckedChange = { fanOn = it })
        }
    }

    Spacer(Modifier.height(12.dp))
    SRSectionCard("Current conditions") {
        Row {
            InfoStat("Temp", "${temp.roundToInt()} °C")
            InfoStat("Humidity", "${rh.roundToInt()} %")
            InfoStat("Since irrigation", "${minutes.roundToInt()} min")
        }
    }

    Spacer(Modifier.height(12.dp))
    val recs = buildList {
        if (mri >= 67) {
            add("Run fan 20–30 min after irrigation; increase airflow.")
            add("Reduce irrigation duration or increase interval.")
        } else if (mri >= 34) {
            add("Add a 10-minute fan cycle after irrigation.")
            add("Avoid standing water on trays; improve drain-off.")
        } else add("Conditions are good. Keep steady airflow across mats.")
    }
    TipsCard(title = "Recommended adjustments", lines = recs)
}

/* ------------------------------ Notes panel ---------------------------- */

@Composable
private fun NotesPanelFancy() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    val notes by prefs.notes.collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { /* preview ignored for now */ }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> images = uris ?: emptyList() }

    ValueHeader(
        icon = Icons.AutoMirrored.Rounded.Notes,
        label = "Notes",
        valueText = "Open",
        gradient = srHeroGradient()
    )

    BigTile(title = "Write a note") {
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = body, onValueChange = { body = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            placeholder = { Text("Type here…") }, label = { Text("Body") }
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { takePicture.launch(null) }) { Text("Take picture") }
            OutlinedButton(onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Add picture") }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    prefs.addNote(title.trim(), body.trim(), images.map { it.toString() })
                    title = ""; body = ""; images = emptyList()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Save") }
    }

    Spacer(Modifier.height(12.dp))

    if (notes.isNotEmpty()) {
        Text("Saved notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes.sortedByDescending { it.createdAt }) { note ->
                NoteItem(note = note, onDelete = { id -> scope.launch { prefs.deleteNote(id) } })
            }
        }
    } else {
        TipsCard(lines = listOf(
            "Use notes to track changes (pump timing, nutrients, cleaning).",
            "Attach images of tray progress for quick visual comparisons."
        ))
    }
}

@Composable
private fun CameraPanel(context: android.content.Context) {
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { /* TODO: feed to analyzer later */ }

    ValueHeader(
        icon = androidx.compose.material.icons.Icons.Rounded.CameraAlt,
        label = "Camera",
        valueText = "Open"
    )

    BigTile(title = "Capture") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scanLauncher.launch(null) }) { Text("Plant scanner") }
            OutlinedButton(onClick = { /* open live feed */ }) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFD32F2F), CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text("Live")
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Used to check plant health or disease.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun NoteItem(note: Note, onDelete: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val thumb = note.imageUris.firstOrNull()
            if (thumb != null) {
                AsyncImage(
                    model = thumb,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(note.title.ifBlank { "Untitled" }, fontWeight = FontWeight.SemiBold)
                Text(
                    if (note.body.isBlank()) "—" else note.body.lines().first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { onDelete(note.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "Delete") }
        }
    }
}

/* ------------------------- Helpers & MRI logic ------------------------- */

private fun computeMoldRiskIndex(tempC: Float, rhPct: Float, minutesSinceIrrigation: Int, fanOn: Boolean): Float {
    val gamma = ln((rhPct / 100f).toDouble()) + (17.62 * tempC) / (243.12 + tempC)
    val td = (243.12 * gamma / (17.62 - gamma)).toFloat()
    val nearWet = (tempC - td) <= 2f || rhPct >= 85f
    val wetScore = when {
        minutesSinceIrrigation < 30 -> 1f
        minutesSinceIrrigation < 120 -> 0.5f
        nearWet -> 0.6f
        else -> 0f
    }
    val humidityScore = ((rhPct - 70f) / 30f).coerceIn(0f, 1f)
    val tempScore = when {
        tempC <= 15f || tempC >= 35f -> 0f
        tempC <= 25f -> ((tempC - 15f) / 10f).coerceIn(0f, 1f)
        else -> ((35f - tempC) / 10f).coerceIn(0f, 1f)
    }
    val airflowPenalty = if (fanOn) 0f else 0.2f
    val raw = 0.4f * humidityScore + 0.3f * tempScore + 0.3f * wetScore
    return ((raw + airflowPenalty) * 100f).coerceIn(0f, 100f)
}

/* -------------------------- Embedded UI kit --------------------------- */

@Composable
private fun SRSectionCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
    modifier: Modifier = Modifier,
    unit: String = "",
    valueColor: Color? = null,
    statusChip: @Composable (() -> Unit)? = null,
    gradient: Brush? = null
) {
    val resolvedGradient = gradient ?: Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary.copy(0.20f), MaterialTheme.colorScheme.primary.copy(0.05f))
    )
    Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 2.dp, modifier = modifier.fillMaxWidth()) {
        Box(Modifier.background(resolvedGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            valueText,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = valueColor ?: MaterialTheme.colorScheme.onSurface
                        )
                        if (unit.isNotBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Text(unit, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Sparkline(values: List<Float>, modifier: Modifier = Modifier) {
    val stroke = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    androidx.compose.foundation.Canvas(
        modifier = modifier.fillMaxWidth().height(120.dp)
    ) {
        val w = size.width
        val h = size.height
        for (i in 1..3) {
            val y = h * i / 4f
            drawLine(grid, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(w, y), strokeWidth = 1f)
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

private fun loadBitmap(context: Context, uri: Uri) =
    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)).asImageBitmap()
