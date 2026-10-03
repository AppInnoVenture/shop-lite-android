package com.yashas.shoplite.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CartState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect the flow directly from Room database
            getCartUseCase().collect { items ->
                _state.update { it.copy(isLoading = false, items = items) }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch { addToCartUseCase(product, quantity) }
    }

    fun increaseQuantity(productId: String) {
        val item = _state.value.items.find { it.productId == productId } ?: return
        viewModelScope.launch { updateCartQuantityUseCase(productId, item.quantity + 1) }
    }

    fun decreaseQuantity(productId: String) {
        val item = _state.value.items.find { it.productId == productId } ?: return
        viewModelScope.launch {
            if (item.quantity > 1) updateCartQuantityUseCase(productId, item.quantity - 1)
            else removeFromCartUseCase(productId)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch { removeFromCartUseCase(productId) }
    }
}