package com.yashas.shoplite.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.CartUseCases
import com.yashas.shoplite.domain.usecase.wishlist.WishlistUseCases
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistUseCases: WishlistUseCases,
    private val cartUseCases: CartUseCases,
    private val currencyUseCase: CurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WishlistState())
    val state: StateFlow<WishlistState> = _state.asStateFlow()
    
    val currency: StateFlow<String> = currencyUseCase.getCurrency().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    init {
        viewModelScope.launch {
            wishlistUseCases.getWishlist().collect { items ->
                _state.value = WishlistState(items = items, isLoading = false)
            }
        }
    }
    
    fun formatPrice(price: Double, curr: String): String {
        return currencyUseCase.formatPrice(price, curr)
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            wishlistUseCases.toggleWishlist(product)
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartUseCases.addToCart(product, 1)
        }
    }
}
