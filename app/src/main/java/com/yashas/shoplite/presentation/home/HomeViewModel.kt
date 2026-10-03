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

enum class SortOption(val displayName: String) {
    NONE("None"),
    RATING("Rating"),
    PRICE_LOW_HIGH("Price (Low to High)"),
    PRICE_HIGH_LOW("Price (High to Low)"),
    DISCOUNT("Discount %")
}

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
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

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoryFlow = MutableStateFlow("All")
    private val sortOptionFlow = MutableStateFlow(SortOption.NONE)
    private val fetchedProductsFlow = MutableStateFlow<List<Product>>(emptyList())
    
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
        // Setup base products flow from DB
        viewModelScope.launch {
            getProductsUseCase().collect { dbProducts ->
                fetchedProductsFlow.value = dbProducts
                
                // Derive categories dynamically from local DB
                val uniqueCategories = dbProducts.map { it.category }.distinct()
                val allCategories = listOf("All") + uniqueCategories.map { it.replaceFirstChar { char -> char.uppercase() } }
                _state.update { it.copy(categories = allCategories, isLoading = false) }
            }
        }
        
        // Initial sync
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getProductsUseCase.sync(forceRefresh = false)
            result.onFailure { error ->
                if (fetchedProductsFlow.value.isEmpty()) {
                    _state.update { it.copy(error = error.message) }
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
        
        // Combine fetched products, category filter, sorting, and search
        viewModelScope.launch {
            combine(
                fetchedProductsFlow,
                selectedCategoryFlow,
                sortOptionFlow,
                searchQueryFlow
            ) { products, category, sortOption, query ->
                var filtered = products
                
                // 1. Category filter
                if (category != "All") {
                    filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
                }
                
                // 2. Search filter by title only (API might return description matches)
                if (query.isNotBlank()) {
                    filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
                }

                // 3. Sorting
                filtered = when (sortOption) {
                    SortOption.NONE -> filtered
                    SortOption.RATING -> filtered.sortedByDescending { it.rating }
                    SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { getDiscountedPrice(it.price, it.discountPercentage) }
                    SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { getDiscountedPrice(it.price, it.discountPercentage) }
                    SortOption.DISCOUNT -> filtered.sortedByDescending { it.discountPercentage }
                }
                
                filtered
            }.collect { displayProducts ->
                _state.update { it.copy(products = displayProducts) }
            }
        }
        
        // Search flow triggering fetch
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    fetchData(query, selectedCategoryFlow.value)
                }
        }
        
        // Category flow triggering fetch
        viewModelScope.launch {
            selectedCategoryFlow
                .drop(1) // Skip initial value
                .collect { category ->
                    fetchData("", category) // Empty query on new category
                }
        }
    }
    
    private suspend fun fetchData(query: String, category: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = if (query.isNotBlank()) {
            searchProductsUseCase(query)
        } else if (category != "All") {
            getProductsByCategoryUseCase(category)
        } else {
            getProductsUseCase.sync(forceRefresh = false)
            // since getProductsUseCase.sync returns Result<Unit>, we just fetch from DB directly
            Result.success(fetchedProductsFlow.value)
        }
        
        result.onSuccess { products ->
            fetchedProductsFlow.value = products
            _state.update { it.copy(isLoading = false) }
        }.onFailure { error ->
            _state.update { it.copy(error = error.message ?: "An unexpected error occurred", isLoading = false) }
        }
    }
    
    private fun getDiscountedPrice(price: Double, discountPercentage: Double): Double {
        return price - (price * (discountPercentage / 100))
    }
    
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = getProductsUseCase.sync(forceRefresh = true)
            result.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
            fetchData(searchQueryFlow.value, selectedCategoryFlow.value)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }
    
    fun onCategorySelected(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        selectedCategoryFlow.value = category
    }
    
    fun onSortOptionSelected(sortOption: SortOption) {
        _state.update { it.copy(selectedSortOption = sortOption) }
        sortOptionFlow.value = sortOption
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
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val selectedSortOption: SortOption = SortOption.NONE
)
