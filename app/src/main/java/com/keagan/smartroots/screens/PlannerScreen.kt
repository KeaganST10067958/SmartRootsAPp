// app/src/main/java/com/keagan/smartroots/screens/PlannerScreen.kt
package com.keagan.smartroots.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // choices
    val vegGroups: List<Pair<String, List<String>>> = listOf(
        "Leafy (mint)" to listOf("Lettuce", "Spinach", "Kale", "Swiss chard"),
        "Herbs (amber)" to listOf("Basil", "Coriander", "Parsley", "Mint"),
        "Fruiting (violet)" to listOf("Tomato (hydro)", "Cucumber (hydro)", "Pepper (hydro)")
    )
    val fodderGroups: List<Pair<String, List<String>>> = listOf(
        "Barley & Wheat (mint)" to listOf("Barley fodder", "Wheat fodder"),
        "Oats & Sorghum (blue)" to listOf("Oats fodder", "Sorghum fodder"),
        "Sunflower & Lucerne (violet)" to listOf("Sunflower fodder", "Lucerne/Alfalfa fodder")
    )

    val initial = if (tent == "veg") savedVeg else savedFodder
    val selected = remember(tent, initial) { mutableStateListOf<String>().apply { addAll(initial) } }
    var saving by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

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
            // Big sticky save button with safe insets
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) { Text(if (saving) "Saving…" else "Save", fontWeight = FontWeight.SemiBold) }
            }
        }
    ) { pads ->
        // The planner content as scrollable "tiles" (cards) above the background
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 120.dp) // room above the bottom bar
        ) {
            item {
                Text(
                    "Pick the crops you plan to grow",
                    style = MaterialTheme.typography.titleMedium,
                    color = scrimFg
                )
            }

            val groups = if (tent == "veg") vegGroups else fodderGroups
            items(groups) { (title, items) ->
                PlannerTile(
                    scrimBg, scrimFg, title = title
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.forEach { name ->
                            val isSelected = name in selected
                            FilterChip(
                                selected = isSelected,
                                onClick = { if (isSelected) selected.remove(name) else selected.add(name) },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }

            item {
                PlannerTile(scrimBg, scrimFg, title = "Color matches") {
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

/** Simple FlowRow without Accompanist */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Minimal reflow: just a Column of Rows measuring maxWidth
    val density = LocalDensity.current
    val screenWidth = with(density) {  // approximation good enough for simple chip groups
        LocalView.current.width.toFloat() / LocalView.current.resources.displayMetrics.density
    }.dp

    Column(modifier, verticalArrangement = verticalArrangement) {
        Row(horizontalArrangement = horizontalArrangement) { content() }
    }
}
