package com.yashas.shoplite.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.AddToCartUseCase
import com.yashas.shoplite.domain.usecase.favorites.GetFavoritesUseCase
import com.yashas.shoplite.domain.usecase.favorites.ToggleFavoriteUseCase
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesState(
    val items: List<Product> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val currencyUseCase: CurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()
    
    val currency: StateFlow<String> = currencyUseCase.getCurrency().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    init {
        viewModelScope.launch {
            getFavoritesUseCase().collect { items ->
                _state.value = FavoritesState(items = items, isLoading = false)
            }
        }
    }
    
    fun formatPrice(price: Double, curr: String): String {
        return currencyUseCase.formatPrice(price, curr)
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product)
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            addToCartUseCase(product, 1)
        }
    }
}
