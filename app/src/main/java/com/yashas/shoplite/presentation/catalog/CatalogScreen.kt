package com.yashas.shoplite.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.yashas.shoplite.presentation.catalog.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    )
) {
    val state by viewModel.state.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val cartItemCount = cartItems.sumOf { it.quantity }
    val cartTotal = cartItems.sumOf { it.price * it.quantity }

    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    var dismissOfflineBanner by rememberSaveable { mutableStateOf(false) }
    var refreshTrigger by rememberSaveable { mutableIntStateOf(0) } // Forces SwipeToDismiss to reset

    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val categoryListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var previousQuery by rememberSaveable { mutableStateOf(state.searchQuery) }
    var previousCategory by rememberSaveable { mutableStateOf(state.selectedCategory) }
    var previousSort by rememberSaveable { mutableStateOf(state.selectedSortOption) }

    LaunchedEffect(state.products) {
        if (previousQuery != state.searchQuery ||
            previousCategory != state.selectedCategory ||
            previousSort != state.selectedSortOption) {

            gridState.scrollToItem(0)

            previousQuery = state.searchQuery
            previousCategory = state.selectedCategory
            previousSort = state.selectedSortOption
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("ShopLite") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onNavigateToWishlist) {
                        Icon(Icons.Default.Favorite, contentDescription = "Wishlist")
                    }
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge { Text(cartItemCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (cartItemCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCart,
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    text = {
                        Text("View Cart ($cartItemCount) • ${viewModel.formatPrice(cartTotal, currency)}")
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection)
                )
        ) {
            // Search Bar (Pinned)
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Content Loading & Error States
            if (!state.isDbInitialized || (state.isLoading && state.products.isEmpty() && !isRefreshing)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!isNetworkAvailable) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Please enable internet connection", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    } else if (state.error != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error ?: "Failed to load products. Please try again.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    } else if (state.searchQuery.isNotEmpty()) {
                        Text("No products found matching '${state.searchQuery}'")
                    } else {
                        Text("No products available.")
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = {
                        dismissOfflineBanner = false
                        refreshTrigger++ // Forces the AnimatedVisibility to recreate the SwipeToDismissBox
                        isRefreshing = true
                        viewModel.refresh()
                        isRefreshing = false
                    },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 0.dp,
                            bottom = padding.calculateBottomPadding() + 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            Column {
                                // Categories
                                LazyRow(
                                    state = categoryListState,
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(state.categories, key = { it }) { category ->
                                        FilterChip(
                                            selected = state.selectedCategory == category,
                                            onClick = { viewModel.onCategorySelected(category) },
                                            label = { Text(category) }
                                        )
                                    }
                                }

                                // Sort Options
                                var expanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text("Sort By: ${state.selectedSortOption.displayName}")
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        SortOption.entries.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option.displayName) },
                                                onClick = {
                                                    viewModel.onSortOptionSelected(option)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Offline indicator
                                LaunchedEffect(isNetworkAvailable) {
                                    if (isNetworkAvailable) dismissOfflineBanner = false
                                }

                                AnimatedVisibility(visible = !isNetworkAvailable && !dismissOfflineBanner) {
                                    // The key here completely resets the swipe-to-dismiss state when refresh is pulled
                                    key(refreshTrigger) {
                                        val dismissState = rememberSwipeToDismissBoxState()
                                        LaunchedEffect(dismissState.currentValue) {
                                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                                dismissOfflineBanner = true
                                            }
                                        }
                                        SwipeToDismissBox(
                                            state = dismissState,
                                            backgroundContent = { Box(Modifier.fillMaxWidth().background(Color.Transparent)) },
                                            content = {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.errorContainer,
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "You are offline. Showing cached products. (Swipe to dismiss)",
                                                        modifier = Modifier.padding(8.dp),
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = wishlistIds.contains(product.id),
                                currency = currency,
                                formatPrice = viewModel::formatPrice,
                                onClick = { onNavigateToProductDetails(product.id) },
                                onAddToCart = { viewModel.addToCart(product) },
                                onToggleFavorite = { viewModel.toggleFavorite(product) }
                            )
                        }

                        if (state.products.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                                ) {
                                    Text(
                                        text = "✨ That's all folks! ✨",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            coroutineScope.launch { gridState.animateScrollToItem(0) }
                                        },
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll up")
                                        Spacer(Modifier.width(8.dp))
                                        Text("Scroll to Top", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}