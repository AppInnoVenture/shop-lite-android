package com.yashas.shoplite.presentation.productdetails

import com.yashas.shoplite.domain.model.Product

data class ProductDetailState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val error: String? = null
)
