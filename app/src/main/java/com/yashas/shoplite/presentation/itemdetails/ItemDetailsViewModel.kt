package com.yashas.shoplite.presentation.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.AddToCartUseCase
import com.yashas.shoplite.domain.usecase.cart.GetCartUseCase
import com.yashas.shoplite.domain.usecase.favorites.GetFavoritesUseCase
import com.yashas.shoplite.domain.usecase.favorites.ToggleFavoriteUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductUseCase
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import com.yashas.shoplite.domain.usecase.cart.UpdateCartQuantityUseCase
import com.yashas.shoplite.domain.usecase.cart.RemoveFromCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val currencyUseCase: CurrencyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(ProductDetailState())
    val state = _state.asStateFlow()
    
    val cartItems = getCartUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val favoriteIds = getFavoritesUseCase.getIds().stateIn(
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
            addToCartUseCase(product)
        }
    }
    
    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                removeFromCartUseCase(productId)
            } else {
                updateCartQuantityUseCase(productId, quantity)
            }
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product)
        }
    }
}

data class ProductDetailState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
