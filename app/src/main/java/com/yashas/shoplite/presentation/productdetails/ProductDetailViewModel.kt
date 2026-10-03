package com.yashas.shoplite.presentation.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yashas.shoplite.domain.usecase.product.GetProductUseCase
import com.yashas.shoplite.navigation.routes.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailState())
    val state = _state.asStateFlow()

    private val productDetailsRoute: Destination.ProductDetails = savedStateHandle.toRoute()

    init {
        loadProduct(productDetailsRoute.productId)
    }

    fun loadProduct(productId: String = productDetailsRoute.productId) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = getProductUseCase(productId)
            result.onSuccess { product ->
                _state.update { it.copy(isLoading = false, product = product) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load product details"
                    )
                }
            }
        }
    }
}
