package com.yashas.shoplite.presentation.home

import com.yashas.shoplite.domain.model.Product

data class HomeState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredProducts: List<Product>
        get() = products.filter { product ->
            val matchesCategory = selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
}
