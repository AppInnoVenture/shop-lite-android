package com.yashas.shoplite.data.remote

import androidx.annotation.Keep

@Keep
data class DimensionsDto(
    val width: Double,
    val height: Double,
    val depth: Double
)
