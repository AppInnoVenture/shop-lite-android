package com.yashas.shoplite.presentation.itemdetails

import com.yashas.shoplite.domain.model.Product

data class ItemDetailsState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)