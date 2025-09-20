package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.R
import com.keagan.smartroots.components.MetricTile
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.model.Metric
import com.keagan.smartroots.ui.SRScaffold

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

    // NOTE: Harvest tile removed here
    val common = listOf(
        Metric("humidity", R.string.humidity, R.string.tip_humidity, "%", 40f, 90f, 55f..75f, Icons.Rounded.WaterDrop),
        Metric("temperature", R.string.temperature, R.string.tip_temperature, "°C", 16f, 34f, 20f..28f, Icons.Rounded.Thermostat),
        Metric("ph", R.string.soil_ph, R.string.tip_ph, "pH", 5.2f, 7.2f, 5.8f..6.5f, Icons.Rounded.Science),
        Metric("ec", R.string.ec, R.string.tip_ec, "mS/cm", 0.2f, 2.5f, 0.4f..1.5f, Icons.Rounded.Bolt),
        Metric("water", R.string.water_level, R.string.tip_water, "%", 20f, 100f, 40f..100f, Icons.Rounded.WaterDrop),
        Metric("notes", R.string.notes, R.string.tip_notes, "", 0f, 0f, null, Icons.AutoMirrored.Rounded.Notes),
        Metric("camera", R.string.camera, R.string.tip_camera, "", 0f, 0f, null, Icons.Rounded.CameraAlt),
    )

    val fodderExtras = listOf(
        Metric("mold", R.string.mold_watch, R.string.tip_mold, "", 0f, 0f, null, Icons.Rounded.Warning),
        Metric("fan", R.string.fan, R.string.tip_fan, "", 0f, 0f, null, Icons.Rounded.Air),
        Metric("irrigation", R.string.irrigation, R.string.tip_irrigation, "", 0f, 0f, null, Icons.Rounded.WaterDrop),
        Metric("light", R.string.light, R.string.tip_light, "", 0f, 0f, null, Icons.Rounded.LightMode)
    )

    val vegExtras = listOf(
        Metric("fan", R.string.fan, R.string.tip_fan, "", 0f, 0f, null, Icons.Rounded.Air),
        Metric("irrigation", R.string.irrigation, R.string.tip_irrigation, "", 0f, 0f, null, Icons.Rounded.WaterDrop),
        Metric("light", R.string.light, R.string.tip_light, "", 0f, 0f, null, Icons.Rounded.LightMode)
    )

    val metrics = (common + if (isVeg) vegExtras else fodderExtras).distinctBy { it.key }

    SRScaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title ${stringResource(R.string.dashboard)}", fontWeight = FontWeight.Medium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null) } }
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
                MetricTile(
                    metric = m,
                    onClick = { onOpenMetric(m.key) } // no harvest branch anymore
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
