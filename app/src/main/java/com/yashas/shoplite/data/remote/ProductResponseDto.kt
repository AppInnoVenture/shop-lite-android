package com.yashas.shoplite.data.remote

import androidx.annotation.Keep

@Keep
data class ProductResponseDto(
    val products: List<ProductDto>
)
