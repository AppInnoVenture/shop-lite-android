package com.yashas.shoplite.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.usecase.cart.CartUseCases
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartUseCases: CartUseCases,
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
            cartUseCases.getCart().collect { items ->
                var subtotal = 0.0
                val cartItems = items.map { item ->
                    subtotal += item.price * item.quantity
                    item
                }
                val deliveryFee = if (subtotal > 0) 10.0 else 0.0
                _state.value = CartState(
                    items = cartItems,
                    subtotal = subtotal,
                    deliveryFee = deliveryFee,
                    total = subtotal + deliveryFee,
                    isLoading = false
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
                cartUseCases.removeFromCart(productId)
            } else {
                cartUseCases.updateCartQuantity(productId, quantity)
            }
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartUseCases.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartUseCases.clearCart()
        }
    }
}
