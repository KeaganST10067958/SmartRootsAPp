package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext              // ✅ fix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.data.Prefs
import kotlinx.coroutines.launch

/* ------------------------- Overlay helper ------------------------- */

@Composable
private fun overlayColors(): Pair<Color, Color> {
    val c = MaterialTheme.colorScheme
    val isLight = c.background.luminance() > 0.5f
    val container = if (isLight) Color.White.copy(alpha = 0.85f)
    else Color(0xFF0E1A0E).copy(alpha = 0.70f)
    val content = if (isLight) Color.Black else Color.White
    return container to content
}

/* ------------------------- Public entry ------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    tent: String,                  // "veg" | "fodder"
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { Prefs(context) }   // ✅ fix: use LocalContext outside remember {}
    val scope = rememberCoroutineScope()

    // Load previously saved selections
    val savedFodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val savedVeg by prefs.selectedVeg.collectAsState(initial = emptyList())

    val initial = if (tent == "veg") savedVeg else savedFodder
    val selected = remember(tent, initial) { mutableStateListOf<String>().apply { addAll(initial) } }

    val snack = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Planner – " + if (tent == "veg") "Vegetables" else "Fodder") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
            )
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            // Big sticky Save button
            Surface(color = Color.Transparent) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                scope.launch {
                                    prefs.saveSelectedCrops(tent = tent, crops = selected.toList())
                                    snack.showSnackbar("Saved ${selected.size} crop${if (selected.size == 1) "" else "s"}")
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(if (isSaving) "Saving…" else "Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { pads ->
        Column(
            Modifier
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme-aware heading
            Text(
                "Pick the crops you plan to grow",
                style = MaterialTheme.typography.titleMedium,
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color.Black else Color.White
            )

            if (tent == "veg") VegGroups(selected) else FodderGroups(selected)

            TipCard(
                lines = listOf(
                    "Crops with the same color can grow together.",
                    "Pick what you want — Save to keep your choices."
                )
            )

            Spacer(Modifier.height(80.dp)) // room above bottom bar
        }
    }
}

/* ------------------------- Chip colors + UI ------------------------- */

private val Mint   = Color(0xFF28B487) // green group
private val Blue   = Color(0xFF2E86DE) // blue group
private val Violet = Color(0xFF7E57C2) // violet group
private val Amber  = Color(0xFFF39C12) // amber group

@Composable
private fun SRChoiceChip(
    text: String,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = c.surface.copy(alpha = 0.72f),
            labelColor = c.onSurface,
            selectedContainerColor = tint.copy(alpha = 0.92f),
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) tint.copy(alpha = 0f) else tint.copy(alpha = 0.70f),
            selectedBorderColor = tint.copy(alpha = 0f),
            disabledSelectedBorderColor = tint.copy(alpha = 0f)
        )
    )
}

/* ------------------------- VEG groups ------------------------- */

@Composable
private fun VegGroups(selected: MutableList<String>) {
    GroupBlock("Leafy (mint)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Lettuce", "Spinach", "Kale", "Swiss chard").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Mint) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
    GroupBlock("Herbs (amber)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Basil", "Coriander", "Parsley", "Mint").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Amber) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
    GroupBlock("Fruiting (violet)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Tomato (hydro)", "Cucumber (hydro)", "Pepper (hydro)").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Violet) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
}

/* ------------------------- FODDER groups ------------------------- */

@Composable
private fun FodderGroups(selected: MutableList<String>) {
    GroupBlock("Barley & Wheat (mint)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Barley fodder", "Wheat fodder").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Mint) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
    GroupBlock("Oats & Sorghum (blue)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Oats fodder", "Sorghum fodder").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Blue) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
    GroupBlock("Sunflower & Lucerne (violet)") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Sunflower fodder", "Lucerne/Alfalfa fodder").forEach { name ->
                val sel = name in selected
                SRChoiceChip(name, sel, Violet) { if (sel) selected.remove(name) else selected.add(name) }
            }
        }
    }
}

/* ------------------------- Small blocks ------------------------- */

@Composable
private fun GroupBlock(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color.Black else Color.White
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun TipCard(
    title: String = "Color matches",             // ✅ default to silence lint
    lines: List<String>
) {
    val (bg, fg) = overlayColors()
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = bg,
            contentColor   = fg
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = fg)
            lines.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = fg) }
        }
    }
}
