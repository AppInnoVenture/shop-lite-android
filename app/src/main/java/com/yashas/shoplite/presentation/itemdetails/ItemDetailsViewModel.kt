package com.yashas.shoplite.presentation.itemdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.CartUseCases
import com.yashas.shoplite.domain.usecase.wishlist.WishlistUseCases
import com.yashas.shoplite.domain.usecase.product.GetProductUseCase
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import dagger.hilt.android.lifecycle.HiltViewModel
@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    private val cartUseCases: CartUseCases,
    private val wishlistUseCases: WishlistUseCases,
    private val currencyUseCase: CurrencyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(ItemDetailsState())
    val state = _state.asStateFlow()
    
    val cartItems = cartUseCases.getCart().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val favoriteIds = wishlistUseCases.getWishlistIds().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val currency = currencyUseCase.getCurrency().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = getProductUseCase(productId)
            result.onSuccess { product ->
                _state.update { it.copy(product = product, isLoading = false) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }
    
    fun formatPrice(price: Double, curr: String): String {
        return currencyUseCase.formatPrice(price, curr)
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartUseCases.addToCart(product)
        }
    }
    
    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                cartUseCases.removeFromCart(productId)
            } else {
                cartUseCases.updateCartQuantity(productId, quantity)
            }
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            wishlistUseCases.toggleWishlist(product)
        }
    }
}

data class ItemDetailsState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
