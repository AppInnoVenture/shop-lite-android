package com.yashas.shoplite.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.usecase.product.GetProductsUseCase
import com.yashas.shoplite.domain.usecase.product.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase // Added here
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = getProductsUseCase()
            result.onSuccess { productList ->
                val distinctCategories = listOf("All") + productList.map { it.category }.distinct().filter { it.isNotBlank() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        products = productList,
                        categories = distinctCategories
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load products"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, isLoading = true) }
        viewModelScope.launch {
            val result = if (query.isBlank()) getProductsUseCase() else searchProductsUseCase(query)
            result.onSuccess { productList ->
                _state.update { it.copy(isLoading = false, products = productList) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onCategorySelect(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }
}