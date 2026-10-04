package com.yashas.shoplite.presentation.catalog

import com.yashas.shoplite.domain.model.Product

data class CatalogState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isDbInitialized: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val selectedSortOption: SortOption = SortOption.NONE
)
