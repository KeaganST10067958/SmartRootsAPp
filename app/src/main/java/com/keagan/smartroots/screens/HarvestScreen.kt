// app/src/main/java/com/keagan/smartroots/screens/HarvestScreen.kt
package com.keagan.smartroots.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.data.Prefs.HarvestBatch
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

@Composable
private fun overlayColors(): Pair<Color, Color> {
    val c = MaterialTheme.colorScheme
    val isLight = c.background.luminance() > 0.5f
    val bg = if (isLight) Color.White.copy(alpha = 0.88f) else Color(0xFF0E1A0E).copy(alpha = 0.72f)
    val fg = if (isLight) Color.Black else Color.White
    return bg to fg
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(ctx) { Prefs(ctx) }
    val scope = rememberCoroutineScope()

    val batches by prefs.harvestBatches.collectAsState(initial = emptyList())
    val fodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val veg by prefs.selectedVeg.collectAsState(initial = emptyList())

    var tab by remember { mutableIntStateOf(if (fodder.isNotEmpty()) 1 else 0) }
    var showAdd by remember { mutableStateOf(false) }
    val (tileBg, tileFg) = overlayColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Guide") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Rounded.Add, null) } },
        containerColor = Color.Transparent
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(color = tileBg, contentColor = tileFg, shape = MaterialTheme.shapes.large) {
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color.Transparent,
                    contentColor = tileFg,
                    divider = {}
                ) {
                    Tab(tab == 0, onClick = { tab = 0 }, text = { Text("VEG") })
                    Tab(tab == 1, onClick = { tab = 1 }, text = { Text("FODDER") })
                }
            }

            val tent = if (tab == 0) "veg" else "fodder"
            val active = batches.filter { it.tent == tent && it.status == "active" }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(
                    top = 12.dp, bottom = 120.dp   // prevents FAB overlap on small phones
                )
            ) {
                if (active.isEmpty()) {
                    item {
                        Surface(color = tileBg, contentColor = tileFg, shape = MaterialTheme.shapes.medium) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) { Text("No active batches. Tap + to add.", color = tileFg) }
                        }
                    }
                } else {
                    items(active, key = { it.id }) { b ->
                        HarvestCard(
                            batch = b,
                            bg = tileBg,
                            fg = tileFg,
                            onHarvest = { scope.launch { prefs.updateBatchStatus(b.id, "harvested") } },
                            onDelete = { scope.launch { prefs.removeBatch(b.id) } }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        val tent = if (tab == 0) "veg" else "fodder"
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

@Composable
private fun HarvestCard(
    batch: HarvestBatch,
    bg: Color,
    fg: Color,
    onHarvest: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysSince = ((now - batch.startEpoch) / (1000L * 60 * 60 * 24)).toInt()
    val left = max(0, batch.daysToHarvest - daysSince)
    val progress = (daysSince.toFloat() / batch.daysToHarvest).coerceIn(0f, 1f)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = bg, contentColor = fg),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(batch.crop, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = fg)
                Spacer(Modifier.weight(1f))
                Text("$left d left", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = fg)
            }
            LinearProgressIndicator(progress = { progress })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Started $daysSince d ago • ${batch.daysToHarvest} d target", color = fg.copy(alpha = 0.85f))
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onHarvest) { Text("Harvest", color = fg) }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
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
                    // Simple cycle selector
                    TextButton(onClick = {
                        val idx = crops.indexOf(crop).takeIf { it >= 0 } ?: -1
                        crop = crops[((idx + 1) % crops.size).coerceAtLeast(0)]
                    }) { Text("Crop: $crop") }
                }
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Days to harvest") })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onCreate(crop, days.toIntOrNull() ?: 7) }, enabled = crop.isNotBlank()) { Text("Add") }
                }
            }
        }
    }
}
