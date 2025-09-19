package com.keagan.smartroots.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

class AppState {
    val sensorEnabled = mutableStateMapOf(
        "humidity" to true,
        "temperature" to true,
        "ph" to true,
        "ec" to true,
        "water" to true
    )
    val fanOn = mutableStateOf(false)
    val irrigationIntervalMin = mutableStateOf(180) // 3h
    val irrigationDurationSec = mutableStateOf(120) // 2m
    val irrigationPaused = mutableStateOf(false)
    val lastIrrigationMs = mutableStateOf(System.currentTimeMillis() - 3_600_000L * 3)
}
