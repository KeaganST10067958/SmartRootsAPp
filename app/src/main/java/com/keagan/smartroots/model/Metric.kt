package com.keagan.smartroots.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Metric(
    val key: String,
    val titleRes: Int,
    val tipRes: Int,
    val unit: String,
    val min: Float,
    val max: Float,
    val ideal: ClosedFloatingPointRange<Float>?,
    val icon: ImageVector
)
