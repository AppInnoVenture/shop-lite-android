package com.yashas.shoplite.presentation.catalog

enum class SortOption(val displayName: String) {
    NONE("None"),
    RATING("Rating"),
    PRICE_LOW_HIGH("Price (Low to High)"),
    PRICE_HIGH_LOW("Price (High to Low)"),
    DISCOUNT("Discount %")
}
