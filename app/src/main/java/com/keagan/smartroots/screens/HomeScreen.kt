package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.R
import com.keagan.smartroots.components.MetricTile
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.model.Metric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mode: String,                    // "veg" | "fodder"
    app: AppState,
    onBack: () -> Unit,
    onOpenMetric: (String) -> Unit
) {
    val isVeg = mode == "veg"
    val title = if (isVeg) stringResource(R.string.vegetables) else stringResource(R.string.fodder)

    val common = listOf(
        Metric("humidity", R.string.humidity, R.string.tip_humidity, "%", 40f, 90f, 55f..75f, Icons.Rounded.WaterDrop),
        Metric("temperature", R.string.temperature, R.string.tip_temperature, "°C", 16f, 34f, 20f..28f, Icons.Rounded.Thermostat),
        Metric("ph", R.string.soil_ph, R.string.tip_ph, "pH", 5.2f, 7.2f, 5.8f..6.5f, Icons.Rounded.Science),
        Metric("ec", R.string.ec, R.string.tip_ec, "mS/cm", 0.2f, 2.5f, 0.4f..1.5f, Icons.Rounded.Bolt),
        Metric("water", R.string.water_level, R.string.tip_water, "%", 20f, 100f, 40f..100f, Icons.Rounded.WaterDrop),
        Metric("harvest", R.string.harvest, R.string.tip_harvest, "", 0f, 0f, null, Icons.Rounded.Spa),
        Metric("notes", R.string.notes, R.string.tip_notes, "", 0f, 0f, null, Icons.AutoMirrored.Rounded.Notes),
        Metric("camera", R.string.camera, R.string.tip_camera, "", 0f, 0f, null, Icons.Rounded.CameraAlt),
    )

    val fodderExtras = listOf(
        Metric("mold", R.string.mold_watch, R.string.tip_mold, "", 0f, 0f, null, Icons.Rounded.Warning),
        Metric("fan", R.string.fan, R.string.tip_fan, "", 0f, 0f, null, Icons.Rounded.Air),
        // keep the key "irrigation" for navigation, but title shows "Pump"
        Metric("irrigation", R.string.irrigation, R.string.tip_irrigation, "", 0f, 0f, null, Icons.Rounded.WaterDrop),
    )

    val extra = listOf(
        Metric("planner", R.string.crop_planner, R.string.tip_crop_planner, "", 0f, 0f, null, Icons.Rounded.List)
    )

    val metrics = (if (isVeg) common else fodderExtras + common) + extra

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title ${stringResource(R.string.dashboard)}", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pads ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(metrics) { m ->
                MetricTile(metric = m, onClick = {
                    when (m.key) {
                        "harvest" -> onOpenMetric("harvest")
                        "planner" -> onOpenMetric("planner") // handled in Nav as planner/{mode}
                        else -> onOpenMetric(m.key)
                    }
                })
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
