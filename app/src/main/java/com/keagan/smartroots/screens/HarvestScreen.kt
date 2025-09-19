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
fun HarvestScreen(onBack: () -> Unit) {
    var crop by remember { mutableStateOf("") }
    val guide = when (crop.trim().lowercase()) {
        "lettuce" -> listOf(
            "Harvest at 30–45 days, heads ~15–20 cm.",
            "EC 0.8–1.2; pH 5.8–6.2; Temp 20–24°C."
        )
        "spinach" -> listOf(
            "Cut outer leaves from 20–25 cm; avoid damaging crown.",
            "EC 1.2–1.8; pH 6.0–6.5; Temp 18–22°C."
        )
        "barley fodder", "fodder" -> listOf(
            "Harvest mats day 7–8 for peak nutrition.",
            "Keep mats moist but free of pooling; steady airflow."
        )
        else -> listOf("Select a crop to see harvest timing and methods.")
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Harvest Guide") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
        )
    }) { pads ->
        Column(
            Modifier
                .padding(pads)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = crop,
                onValueChange = { crop = it },
                label = { Text("Crop name") },
                modifier = Modifier.fillMaxWidth()
            )
            TipsCard(title = "Guide", lines = guide)
        }
    }
}
