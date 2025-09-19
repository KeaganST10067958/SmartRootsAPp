package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.widgets.TipsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPlannerScreen(mode: String, onBack: () -> Unit) {
    val optionsVeg = listOf("Lettuce", "Spinach", "Basil", "Kale")
    val optionsFodder = listOf("Barley fodder", "Wheat fodder", "Oats fodder")
    val options = if (mode == "veg") optionsVeg else optionsFodder

    val selected = remember { mutableStateListOf<String>() }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Crop Planner (${mode.uppercase()})") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
        )
    }) { pads ->
        Column(
            Modifier
                .padding(pads)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pick what you want to grow", style = MaterialTheme.typography.titleMedium)

            // Simple vertical list of chips (no Accompanist needed)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { o ->
                    val checked = o in selected
                    FilterChip(
                        selected = checked,
                        onClick = { if (checked) selected.remove(o) else selected.add(o) },
                        label = { Text(o) }
                    )
                }
            }

            if (selected.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                val (compat, params) = compatibilityFor(selected, mode)
                TipsCard(title = "Compatibility", lines = compat)
                Spacer(Modifier.height(8.dp))
                TipsCard(title = "Parameters", lines = params)
            }
        }
    }
}

private fun compatibilityFor(crops: List<String>, mode: String): Pair<List<String>, List<String>> {
    val compat = mutableListOf<String>()
    val params = mutableListOf<String>()

    if (mode == "veg") {
        if (crops.containsAll(listOf("Lettuce", "Basil")))
            compat += "Lettuce + Basil: OK at EC 1.0–1.4, pH 5.8–6.2."
        if (crops.contains("Kale") && crops.contains("Basil"))
            compat += "Kale + Basil: watch temp; kale prefers cooler."
        params += "Typical veg: EC 0.8–1.6, pH 5.8–6.2, 20–26°C, RH 55–70%."
    } else {
        compat += "Fodder mixes are fine; align germination schedules."
        params += "Fodder: EC 0.4–1.0, pH 5.8–6.2, 18–24°C, frequent short pumps."
    }

    if (compat.isEmpty()) compat += "No conflicts detected."
    return compat to params
}
