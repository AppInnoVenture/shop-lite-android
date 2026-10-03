package com.yashas.shoplite.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.data.util.NetworkConnectivityManager
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.AddToCartUseCase
import com.yashas.shoplite.domain.usecase.cart.GetCartUseCase
import com.yashas.shoplite.domain.usecase.wishlist.GetWishlistUseCase
import com.yashas.shoplite.domain.usecase.wishlist.ToggleWishlistUseCase
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
import kotlin.time.Duration.Companion.milliseconds

enum class SortOption(val displayName: String) {
    NONE("None"),
    RATING("Rating"),
    PRICE_LOW_HIGH("Price (Low to High)"),
    PRICE_HIGH_LOW("Price (High to Low)"),
    DISCOUNT("Discount %")
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getWishlistUseCase: GetWishlistUseCase,
    private val toggleWishlistUseCase: ToggleWishlistUseCase,
    private val currencyUseCase: CurrencyUseCase,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
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
    
    val wishlistIds = getWishlistUseCase.getIds().stateIn(
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
        val debouncedSearchQuery = searchQueryFlow
            .debounce(600.milliseconds)
            .distinctUntilChanged()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val cacheValid = getProductsUseCase.isCacheValid()
            val networkAvailable = isNetworkAvailable.value
            
            if (!cacheValid && networkAvailable) {
                val result = getProductsUseCase.sync(forceRefresh = true)
                if (result.isFailure && fetchedProductsFlow.value.isEmpty()) {
                    _state.update { it.copy(error = result.exceptionOrNull()?.message) }
                }
            } else {
                launch { 
                    getProductsUseCase.sync(forceRefresh = false) 
                }
            }
            
            // Now start observing DB
            getProductsUseCase().collect { dbProducts ->
                fetchedProductsFlow.value = dbProducts
                
                // Derive categories dynamically from local DB
                val uniqueCategories = dbProducts.map { it.category }.distinct().sorted()
                val allCategories = listOf("All") + uniqueCategories.map { it.replaceFirstChar { char -> char.uppercase() } }
                _state.update { it.copy(categories = allCategories, isDbInitialized = true, isLoading = false) }
            }
        }
        
        // Combine fetched products, category filter, sorting, and search
        viewModelScope.launch {
            combine(
                fetchedProductsFlow,
                selectedCategoryFlow,
                sortOptionFlow,
                debouncedSearchQuery
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
            debouncedSearchQuery.collect { query ->
                fetchData(query, selectedCategoryFlow.value)
            }
        }
        
        // Category flow triggering fetch
        viewModelScope.launch {
            selectedCategoryFlow
                .drop(1) // Skip initial value
                .collect { category ->
                    fetchData(searchQueryFlow.value, category)
                }
        }
    }
    
    private suspend fun fetchData(query: String, category: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            if (query.isNotBlank()) {
                val res = searchProductsUseCase(query)
                if (res.isFailure) throw res.exceptionOrNull() ?: Exception("Search failed")
            } else if (category != "All") {
                val res = getProductsByCategoryUseCase(category)
                if (res.isFailure) throw res.exceptionOrNull() ?: Exception("Category fetch failed")
            } else {
                val res = getProductsUseCase.sync(forceRefresh = false)
                if (res.isFailure) throw res.exceptionOrNull() ?: Exception("Sync failed")
            }
            _state.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message ?: "An unexpected error occurred", isLoading = false) }
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
            toggleWishlistUseCase(product)
        }
    }
}

data class CatalogState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isDbInitialized: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val selectedSortOption: SortOption = SortOption.NONE
)
