package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.widgets.TipsCard
import kotlinx.coroutines.launch

private data class CropOption(val name: String, val group: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    tent: String,
    onBack: () -> Unit
) {
    val normalizedTent = if (tent.equals("veg", true)) "veg" else "fodder"
    val title = if (normalizedTent == "veg") "Crop Planner – Vegetables" else "Crop Planner – Fodder"

    // ---- Options (group = same-colour = grow well together) ----
    val vegOptions = remember {
        listOf(
            // group 1
            CropOption("Lettuce", 1), CropOption("Spinach", 1), CropOption("Basil", 1),
            CropOption("Rocket/Arugula", 1),
            // group 2
            CropOption("Kale", 2), CropOption("Swiss chard", 2), CropOption("Parsley", 2),
            CropOption("Coriander", 2),
            // group 3 (fruiting hydro)
            CropOption("Tomato (hydro)", 3), CropOption("Cherry tomato (hydro)", 3),
            CropOption("Cucumber (hydro)", 3), CropOption("Pepper/Chilli (hydro)", 3),
            // group 4 (cool herbs/brassicas)
            CropOption("Mint", 4), CropOption("Chives", 4), CropOption("Dill", 4), CropOption("Pak choi", 4),
        )
    }
    val fodderOptions = remember {
        listOf(
            CropOption("Barley fodder", 1), CropOption("Wheat fodder", 1),
            CropOption("Oats fodder", 2),   CropOption("Maize fodder", 2),
            CropOption("Sorghum fodder", 3), CropOption("Sunflower fodder", 3),
            CropOption("Lucerne/Alfalfa fodder", 4), CropOption("Ryegrass fodder", 4)
        )
    }
    val options = if (normalizedTent == "veg") vegOptions else fodderOptions

    // ---- Persistence ----
    val ctx = LocalContext.current
    val prefs = remember(ctx) { Prefs(ctx) }
    val savedVeg by prefs.selectedVeg.collectAsState(initial = emptyList())
    val savedFodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val saved = if (normalizedTent == "veg") savedVeg else savedFodder
    val scope = rememberCoroutineScope()

    var selected by rememberSaveable(normalizedTent) { mutableStateOf(saved) }
    LaunchedEffect(saved) { selected = saved }

    // ---- High-contrast swatches for groups ----
    val groupSwatches = listOf(
        Color(0xFF7E57C2), // 1: Purple
        Color(0xFF42A5F5), // 2: Blue
        Color(0xFF26A69A), // 3: Teal
        Color(0xFFFFB300), // 4: Amber
        Color(0xFFEF5350), // 5: Red (spare)
        Color(0xFF66BB6A)  // 6: Green (spare)
    )
    @Composable
    fun chipColorsFor(group: Int, isSelected: Boolean): SelectableChipColors {
        val base = groupSwatches[(group - 1).coerceAtLeast(0) % groupSwatches.size]
        val container = if (isSelected) base else base.copy(alpha = 0.18f)
        val label = if (isSelected) Color.White else base
        return FilterChipDefaults.filterChipColors(
            containerColor = container,
            labelColor = label,
            selectedContainerColor = base,
            selectedLabelColor = Color.White,
            iconColor = label,
            selectedLeadingIconColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { scope.launch { prefs.saveSelectedCrops(normalizedTent, selected) } },
                        enabled = selected.isNotEmpty()
                    ) { Text("Save") }
                }
            )
        }
    ) { pads ->
        Column(
            modifier = Modifier
                .padding(pads)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pick the crops you plan to grow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Grid = cleaner, less clutter
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(options, key = { it.name }) { opt ->
                    val isSelected = opt.name in selected
                    val swatch = groupSwatches[(opt.group - 1) % groupSwatches.size]
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selected = if (isSelected) selected - opt.name else selected + opt.name
                        },
                        label = { Text(opt.name) },
                        colors = chipColorsFor(opt.group, isSelected),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = swatch.copy(0.40f),
                            selectedBorderColor = Color.White // pop on dark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (selected.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.elevatedCardElevation(3.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Selected (${selected.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(selected.sorted().joinToString(", "))
                    }
                }
            }

            // One clear tip replacing compatibility paragraphs
            TipsCard(
                title = "Tip",
                lines = listOf(
                    "Same-colour crops grow well together.",
                    "You can mix colours too—just keep EC, pH and light suitable for all."
                )
            )

            val settings = if (normalizedTent == "veg")
                "Leafy veg: EC 0.8–1.6 • pH 5.8–6.2 • Temp 20–26°C • RH 55–70%."
            else
                "Fodder: EC 0.4–1.0 • pH 5.8–6.2 • Temp 18–24°C • short, frequent pumps."
            TipsCard(title = "Target settings", lines = listOf(settings))
        }
    }
}
