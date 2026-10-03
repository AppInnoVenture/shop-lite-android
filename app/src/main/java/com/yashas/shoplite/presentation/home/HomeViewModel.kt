package com.yashas.shoplite.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.data.util.NetworkConnectivityManager
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.AddToCartUseCase
import com.yashas.shoplite.domain.usecase.cart.GetCartUseCase
import com.yashas.shoplite.domain.usecase.favorites.GetFavoritesUseCase
import com.yashas.shoplite.domain.usecase.favorites.ToggleFavoriteUseCase
import com.yashas.shoplite.domain.usecase.product.GetCategoriesUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductsByCategoryUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductsUseCase
import com.yashas.shoplite.domain.usecase.product.SearchProductsUseCase
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val currencyUseCase: CurrencyUseCase,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    
    val isNetworkAvailable = networkConnectivityManager.isNetworkAvailable.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
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
    
    fun formatPrice(price: Double, curr: String): String {
        return currencyUseCase.formatPrice(price, curr)
    }

    init {
        loadData()
        
        viewModelScope.launch {
            searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else if (_state.value.selectedCategory != "All") {
                        loadCategory(_state.value.selectedCategory)
                    } else {
                        loadProducts()
                    }
                }
        }
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Load categories first
            val categoriesResult = getCategoriesUseCase()
            categoriesResult.onSuccess { categories ->
                val allCategories = listOf("All") + categories.map { it.replaceFirstChar { char -> char.uppercase() } }
                _state.update { it.copy(categories = allCategories) }
            }
            
            loadProducts(forceRefresh)
        }
    }
    
    fun refresh() {
        loadData(forceRefresh = true)
    }

    private suspend fun loadProducts(forceRefresh: Boolean = false) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = getProductsUseCase(forceRefresh)
        result.onSuccess { products ->
            _state.update { it.copy(products = products, isLoading = false) }
        }.onFailure { error ->
            _state.update { it.copy(error = error.message ?: "An unexpected error occurred", isLoading = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    private suspend fun performSearch(query: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = searchProductsUseCase(query)
        result.onSuccess { products ->
            _state.update { it.copy(products = products, isLoading = false) }
        }.onFailure { error ->
            _state.update { it.copy(error = error.message ?: "An unexpected error occurred", isLoading = false) }
        }
    }
    
    fun onCategorySelected(category: String) {
        _state.update { it.copy(selectedCategory = category, searchQuery = "") }
        searchQuery.value = ""
        viewModelScope.launch {
            if (category == "All") {
                loadProducts()
            } else {
                loadCategory(category.lowercase())
            }
        }
    }
    
    private suspend fun loadCategory(category: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = getProductsByCategoryUseCase(category)
        result.onSuccess { products ->
            _state.update { it.copy(products = products, isLoading = false) }
        }.onFailure { error ->
            _state.update { it.copy(error = error.message ?: "An unexpected error occurred", isLoading = false) }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            addToCartUseCase(product)
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product)
        }
    }
}

data class HomeState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All"
)
