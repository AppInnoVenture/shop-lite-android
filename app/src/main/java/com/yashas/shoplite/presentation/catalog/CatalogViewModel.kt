package com.yashas.shoplite.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashas.shoplite.data.util.NetworkConnectivityManager
import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.usecase.cart.CartUseCases
import com.yashas.shoplite.domain.usecase.catalog.CatalogUseCases
import com.yashas.shoplite.domain.usecase.wishlist.WishlistUseCases
import com.yashas.shoplite.domain.usecase.settings.CurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogUseCases: CatalogUseCases,
    private val cartUseCases: CartUseCases,
    private val wishlistUseCases: WishlistUseCases,
    private val currencyUseCase: CurrencyUseCase,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
    val state = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoryFlow = MutableStateFlow("All")
    private val sortOptionFlow = MutableStateFlow(SortOption.NONE)
    private val fetchedProductsFlow = MutableStateFlow<List<Product>>(emptyList())
    private val dbLoadedFlow = MutableStateFlow(false) // Tracks if Room has officially emitted

    val isNetworkAvailable = networkConnectivityManager.isNetworkAvailable.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val cartItems = cartUseCases.getCart().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val wishlistIds = wishlistUseCases.getWishlistIds().stateIn(
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

            val cacheValid = catalogUseCases.getProducts.isCacheValid()
            val networkAvailable = isNetworkAvailable.value

            if (!cacheValid && networkAvailable) {
                val result = catalogUseCases.getProducts.sync(forceRefresh = true)
                if (result.isFailure && fetchedProductsFlow.value.isEmpty()) {
                    _state.update { it.copy(error = result.exceptionOrNull()?.message) }
                }
            } else {
                launch {
                    catalogUseCases.getProducts.sync(forceRefresh = false)
                }
            }

            // Observe DB and flag when it has emitted
            catalogUseCases.getProducts().collect { dbProducts ->
                fetchedProductsFlow.value = dbProducts

                val uniqueCategories = dbProducts.map { it.category }.distinct().sorted()
                val allCategories = listOf("All") + uniqueCategories.map { it.replaceFirstChar { char -> char.uppercase() } }
                _state.update { it.copy(categories = allCategories) }

                dbLoadedFlow.value = true // DB is officially loaded
            }
        }

        // Combine fetched products, category filter, sorting, and search
        viewModelScope.launch {
            combine(
                fetchedProductsFlow,
                selectedCategoryFlow,
                sortOptionFlow,
                debouncedSearchQuery,
                dbLoadedFlow
            ) { products, category, sortOption, query, isDbLoaded ->
                if (!isDbLoaded) return@combine null // PREVENTS THE FLASH! Halts until DB returns data

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
            }.filterNotNull().collect { displayProducts ->
                _state.update { it.copy(
                    products = displayProducts,
                    isDbInitialized = true,
                    isLoading = false
                ) }
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
                .drop(1)
                .collect { category ->
                    fetchData(searchQueryFlow.value, category)
                }
        }
        
        // Auto-refresh when network comes back online
        viewModelScope.launch {
            var wasOffline = false
            isNetworkAvailable.collect { isAvailable ->
                if (isAvailable && wasOffline) {
                    if (fetchedProductsFlow.value.isEmpty() || _state.value.error != null) {
                        // kotlinx.coroutines.delay(1000.milliseconds) // Give DNS a moment to settle
                        refresh()
                    }
                }
                wasOffline = !isAvailable
            }
        }
    }

    private suspend fun fetchData(query: String, category: String) {
        if (!isNetworkAvailable.value) {
            _state.update { it.copy(isLoading = false) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        try {
            if (query.isNotBlank()) {
                val res = catalogUseCases.searchProducts(query)
                if (res.isFailure) throw res.exceptionOrNull() ?: Exception("Search failed")
            } else if (category != "All") {
                val res = catalogUseCases.getProductsByCategory(category)
                if (res.isFailure) throw res.exceptionOrNull() ?: Exception("Category fetch failed")
            } else {
                val res = catalogUseCases.getProducts.sync(forceRefresh = false)
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
        if (!isNetworkAvailable.value) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = catalogUseCases.getProducts.sync(forceRefresh = true)
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
            cartUseCases.addToCart(product)
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            wishlistUseCases.toggleWishlist(product)
        }
    }
}