package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.data.Prefs.HarvestBatch
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import androidx.compose.material.icons.rounded.Delete


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()

    val batches by prefs.harvestBatches.collectAsState(initial = emptyList())
    val fodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val veg by prefs.selectedVeg.collectAsState(initial = emptyList())

    var tent by remember { mutableStateOf(if (fodder.isNotEmpty()) "fodder" else "veg") }
    val selectedList = if (tent == "fodder") fodder else veg
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Guide") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                actions = {
                    AssistChip(
                        onClick = { tent = if (tent == "fodder") "veg" else "fodder" },
                        label = { Text(if (tent == "fodder") "FODDER" else "VEG") }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add batch")
            }
        }
    ) { pads ->
        Column(Modifier.fillMaxSize().padding(pads).padding(16.dp)) {
            if (selectedList.isEmpty()) {
                Text("No crops selected in the ${tent.uppercase()} planner.", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Open the planner from the welcome screen to choose crops.")
            } else {
                val active = batches.filter { it.tent == tent && it.status == "active" }
                if (active.isEmpty()) {
                    Text("No active batches. Tap + to start tracking.", fontWeight = FontWeight.SemiBold)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(active, key = { it.id }) { b ->
                            HarvestCard(
                                batch = b,
                                onHarvest = { scope.launch { prefs.updateBatchStatus(b.id, "harvested") } },
                                onRemove = { scope.launch { prefs.removeBatch(b.id) } }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddBatchDialog(
            tent = tent,
            crops = selectedList,
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
    batch: Prefs.HarvestBatch,
    onHarvest: () -> Unit,
    onRemove: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysSinceStart = ((now - batch.startEpoch) / (1000L * 60 * 60 * 24)).toInt()
    val left = kotlin.math.max(0, batch.daysToHarvest - daysSinceStart)
    val progress = (daysSinceStart.toFloat() / batch.daysToHarvest).coerceIn(0f, 1f)

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(batch.crop, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("$left d left", style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(progress = { progress })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Started $daysSinceStart d ago • Target ${batch.daysToHarvest} d",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onHarvest) { Text("Harvest") }
                // 🔻 Changed: bin icon instead of "Remove" text
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Delete,
                        contentDescription = "Remove batch",
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
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onCreate(crop, days.toIntOrNull() ?: 7) }, enabled = crop.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (crops.isEmpty()) {
                    Text("No crops in ${tent.uppercase()} planner.")
                } else {
                    OutlinedTextField(value = crop, onValueChange = { crop = it }, label = { Text("Crop") })
                }
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Days to harvest") })
                Text("Defaults: fodder ~8–10 d, leafy veg varies by crop.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
