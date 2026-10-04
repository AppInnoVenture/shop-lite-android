package com.yashas.shoplite.presentation.wishlist

import com.yashas.shoplite.domain.model.Product

data class WishlistState(
    val items: List<Product> = emptyList(),
    val isLoading: Boolean = true
)