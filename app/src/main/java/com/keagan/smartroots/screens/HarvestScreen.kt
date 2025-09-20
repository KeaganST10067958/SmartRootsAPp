package com.keagan.smartroots.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext          // ✅ missing import fixed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.data.Prefs.HarvestBatch
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

/* --------------------------- Helpers --------------------------- */

@Composable
private fun overlayColors(): Pair<Color, Color> {
    val c = MaterialTheme.colorScheme
    val isLight = c.background.luminance() > 0.5f
    val container = if (isLight) Color.White.copy(alpha = 0.85f)
    else Color(0xFF0E1A0E).copy(alpha = 0.70f)
    val content = if (isLight) Color.Black else Color.White
    return container to content
}

/* --------------------------- Screen ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember(ctx) { Prefs(ctx) }
    val scope = rememberCoroutineScope()

    val batches by prefs.harvestBatches.collectAsState(initial = emptyList())
    val fodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val veg by prefs.selectedVeg.collectAsState(initial = emptyList())

    var tentTab by remember {
        mutableIntStateOf(if (fodder.isNotEmpty()) 1 else 0) // 0 = veg, 1 = fodder
    }
    var showAdd by remember { mutableStateOf(false) }

    val (overlayBg, overlayFg) = overlayColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add batch")
            }
        },
        containerColor = Color.Transparent
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simple two-tab switcher (VEG / FODDER)
            Surface(
                color = overlayBg,
                contentColor = overlayFg,
                shape = MaterialTheme.shapes.medium
            ) {
                TabRow(
                    selectedTabIndex = tentTab,
                    containerColor = Color.Transparent,
                    contentColor = overlayFg,
                    divider = {}
                ) {
                    Tab(selected = tentTab == 0, onClick = { tentTab = 0 }, text = { Text("VEG") })
                    Tab(selected = tentTab == 1, onClick = { tentTab = 1 }, text = { Text("FODDER") })
                }
            }

            val activeTent = if (tentTab == 0) "veg" else "fodder"
            val active = batches.filter { it.tent == activeTent && it.status == "active" }

            if (active.isEmpty()) {
                EmptyState(
                    text = "No active batches. Tap + to start tracking.",
                    bg = overlayBg, fg = overlayFg
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(active, key = { it.id }) { b ->
                        HarvestCard(
                            batch = b,
                            onHarvest = {
                                scope.launch { prefs.updateBatchStatus(b.id, "harvested") }
                            },
                            onRemove = {
                                scope.launch { prefs.removeBatch(b.id) }
                            },
                            bg = overlayBg,
                            fg = overlayFg
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        val tent = if (tentTab == 0) "veg" else "fodder"
        val crops = if (tent == "veg") veg else fodder
        AddBatchDialog(
            tent = tent,
            crops = crops,
            onDismiss = { showAdd = false },
            onCreate = { crop, days ->
                val batch = HarvestBatch(
                    id = UUID.randomUUID().toString(),
                    tent = tent,
                    crop = crop,
                    startEpoch = System.currentTimeMillis(),
                    daysToHarvest = days
                )
                scope.launch { prefs.addHarvestBatch(batch) }
                showAdd = false
            }
        )
    }
}

/* -------------------------- Components ------------------------- */

@Composable
private fun EmptyState(text: String, bg: Color, fg: Color) {
    Surface(
        color = bg, contentColor = fg,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = fg)
        }
    }
}

@Composable
private fun HarvestCard(
    batch: HarvestBatch,
    onHarvest: () -> Unit,
    onRemove: () -> Unit,
    bg: Color,
    fg: Color
) {
    val now = System.currentTimeMillis()
    val daysSinceStart = ((now - batch.startEpoch) / (1000L * 60 * 60 * 24)).toInt()
    val left = max(0, batch.daysToHarvest - daysSinceStart)
    val progress = (daysSinceStart.toFloat() / batch.daysToHarvest).coerceIn(0f, 1f)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = bg, contentColor = fg),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    batch.crop,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = fg
                )
                Spacer(Modifier.weight(1f))
                Text("$left d left", style = MaterialTheme.typography.titleMedium, color = fg)
            }
            LinearProgressIndicator(progress = { progress })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Started $daysSinceStart d ago • ${batch.daysToHarvest} d target",
                    color = fg.copy(alpha = 0.85f)
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onHarvest) { Text("Harvest", color = fg) }
                TextButton(onClick = onRemove) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddBatchDialog(
    tent: String,
    crops: List<String>,
    onDismiss: () -> Unit,
    onCreate: (crop: String, days: Int) -> Unit
) {
    var crop by remember { mutableStateOf(crops.firstOrNull().orEmpty()) }
    var days by remember { mutableStateOf(if (tent == "fodder") "8" else "45") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 3.dp) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Add batch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (crops.isEmpty()) {
                    Text("No crops in ${tent.uppercase()} planner.")
                } else {
                    // Simple dropdown replacement – click to cycle choices
                    Text(
                        text = "Crop: $crop",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val idx = crops.indexOf(crop).takeIf { it >= 0 } ?: -1
                                crop = crops[((idx + 1) % crops.size).coerceAtLeast(0)]
                            }
                            .padding(8.dp)
                    )
                }
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Days to harvest") }
                )
                Text("Defaults: fodder ~8–10 d, leafy veg varies by crop.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onCreate(crop, days.toIntOrNull() ?: 7) },
                        enabled = crop.isNotBlank()
                    ) { Text("Add") }
                }
            }
        }
    }
}
