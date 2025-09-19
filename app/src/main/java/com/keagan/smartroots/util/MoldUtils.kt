package com.keagan.smartroots.util

import kotlin.math.ln
import kotlin.math.roundToInt

fun computeMoldRiskIndex(
    tempC: Float,
    rh: Float,
    minutesSinceIrrigation: Int,
    fanOn: Boolean
): Float {
    val gamma = ln((rh / 100f).toDouble()) + (17.62 * tempC) / (243.12 + tempC)
    val td = (243.12 * gamma / (17.62 - gamma)).toFloat()
    val nearWet = (tempC - td) <= 2f || rh >= 85f

    val wetScore = when {
        minutesSinceIrrigation < 30 -> 1f
        minutesSinceIrrigation < 120 -> 0.5f
        nearWet -> 0.6f
        else -> 0f
    }
    val humidityScore = ((rh - 70f) / 30f).coerceIn(0f, 1f)
    val tempScore = when {
        tempC <= 15f || tempC >= 35f -> 0f
        tempC <= 25f -> ((tempC - 15f) / 10f).coerceIn(0f, 1f)
        else -> ((35f - tempC) / 10f).coerceIn(0f, 1f)
    }
    val airflowPenalty = if (fanOn) 0f else 0.2f
    val raw = 0.4f * humidityScore + 0.3f * tempScore + 0.3f * wetScore
    return ((raw + airflowPenalty) * 100f).coerceIn(0f, 100f)
}

fun adviceForMold(mri: Float): String = when {
    mri >= 67 -> "High risk: Increase airflow, shorten watering, and check drainage."
    mri >= 34 -> "Caution: Watch humidity, consider a brief fan boost after irrigation."
    else -> "OK: Conditions are good. Keep airflow steady."
}

fun Float.pretty(): String = this.roundToInt().toString()
