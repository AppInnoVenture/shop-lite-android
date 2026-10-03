package com.yashas.shoplite.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.usecase.cart.ClearCartUseCase
import com.yashas.shoplite.domain.usecase.cart.GetCartUseCase
import com.yashas.shoplite.domain.usecase.cart.RemoveFromCartUseCase
import com.yashas.shoplite.domain.usecase.cart.UpdateCartQuantityUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductUseCase
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val currencyUseCase: CurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

    val currency = currencyUseCase.getCurrency().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    init {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                var subtotal = 0.0
                val cartItems = items.map { item ->
                    val productResult = getProductUseCase(item.productId)
                    val product = productResult.getOrNull()
                    val isOutOfStock = product == null || product.stock == 0
                    
                    if (!isOutOfStock) {
                        subtotal += item.price * item.quantity
                    }
                    
                    item.copy(isOutOfStock = isOutOfStock)
                }
                val deliveryFee = if (subtotal > 0) 10.0 else 0.0
                _state.value = CartState(
                    items = cartItems,
                    subtotal = subtotal,
                    deliveryFee = deliveryFee,
                    total = subtotal + deliveryFee
                )
            }
        }
    }

    fun formatPrice(price: Double, curr: String): String {
        return currencyUseCase.formatPrice(price, curr)
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                removeFromCartUseCase(productId)
            } else {
                updateCartQuantityUseCase(productId, quantity)
            }
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            removeFromCartUseCase(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
        }
    }
}
