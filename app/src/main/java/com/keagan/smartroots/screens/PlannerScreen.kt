// app/src/main/java/com/keagan/smartroots/screens/PlannerScreen.kt
package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.data.Prefs
import kotlinx.coroutines.launch

@Composable
private fun overlayColors(): Pair<Color, Color> {
    val cs = MaterialTheme.colorScheme
    val isLight = cs.background.luminance() > 0.5f
    val container = if (isLight) Color.White.copy(alpha = 0.88f) else Color(0xFF0E1A0E).copy(alpha = 0.72f)
    val content = if (isLight) Color.Black else Color.White
    return container to content
}

/** Map the tag inside parentheses to a chip color palette. */
@Composable
private fun paletteFor(tag: String): Pair<Color, Color> = when (tag.lowercase()) {
    "mint"   -> Color(0xFF2EBE7E) to Color.White   // green
    "amber"  -> Color(0xFFF39C12) to Color(0xFF1A1000)
    "violet" -> Color(0xFF7A6BF2) to Color.White
    "blue"   -> Color(0xFF3F51B5) to Color.White
    else     -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
}

/** Chip colors per tag. */
@Composable
private fun chipColors(tag: String) =
    FilterChipDefaults.filterChipColors(
        containerColor = paletteFor(tag).first.copy(alpha = 0.12f),
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = paletteFor(tag).first,
        selectedLabelColor = paletteFor(tag).second
    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(
    tent: String,              // "veg" | "fodder"
    onBack: () -> Unit
) {
    val ctx = LocalView.current.context
    val prefs = remember(ctx) { Prefs(ctx) }
    val scope = rememberCoroutineScope()

    val savedFodder by prefs.selectedFodder.collectAsState(initial = emptyList())
    val savedVeg by prefs.selectedVeg.collectAsState(initial = emptyList())
    val (scrimBg, scrimFg) = overlayColors()

    // Choices (keep the tags in parentheses: mint / amber / violet / blue)
    val vegGroups: List<Pair<String, List<String>>> = listOf(
        "Leafy (mint)"      to listOf("Lettuce", "Spinach", "Kale", "Swiss chard"),
        "Herbs (amber)"     to listOf("Basil", "Coriander", "Parsley", "Mint"),
        "Fruiting (violet)" to listOf("Tomato (hydro)", "Cucumber (hydro)", "Pepper (hydro)")
    )
    val fodderGroups: List<Pair<String, List<String>>> = listOf(
        "Barley & Wheat (mint)"      to listOf("Barley fodder", "Wheat fodder"),
        "Oats & Sorghum (blue)"      to listOf("Oats fodder", "Sorghum fodder"),
        "Sunflower & Lucerne (violet)" to listOf("Sunflower fodder", "Lucerne/Alfalfa fodder")
    )

    val initial = if (tent == "veg") savedVeg else savedFodder
    val selected = remember(tent, initial) { mutableStateListOf<String>().apply { addAll(initial) } }
    var saving by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    // Flat list of (crop, tag)
    val groups = if (tent == "veg") vegGroups else fodderGroups
    val cropsWithTags: List<Pair<String, String>> = remember(groups) {
        groups.flatMap { (title, items) ->
            val tag = title.substringAfter('(').substringBefore(')').lowercase()
            items.map { it to tag }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Planner – ${if (tent == "veg") "Vegetables" else "Fodder"}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (!saving) {
                            saving = true
                            scope.launch {
                                prefs.saveSelectedCrops(tent, selected.toList())
                                snackbar.showSnackbar("Saved ${selected.size} crop${if (selected.size == 1) "" else "s"}")
                                saving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) { Text(if (saving) "Saving…" else "Save", fontWeight = FontWeight.SemiBold) }
            }
        }
    ) { pads ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Text(
                    "Pick the crops you plan to grow",
                    style = MaterialTheme.typography.titleMedium,
                    color = scrimFg
                )
            }

            item {
                PlannerTile(
                    container = scrimBg,
                    contentColor = scrimFg,
                    title = if (tent == "veg") "Vegetables" else "Fodder"
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        cropsWithTags.forEach { (name, tag) ->
                            val isSelected = name in selected
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selected.remove(name) else selected.add(name)
                                },
                                label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = chipColors(tag)
                            )
                        }
                    }
                }
            }

            item {
                PlannerTile(container = scrimBg, contentColor = scrimFg, title = "Tips") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Crops with the same color can grow together.")
                        Text("• Pick what you want — Save to keep your choices.")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerTile(
    container: Color,
    contentColor: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = container, contentColor = contentColor),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
