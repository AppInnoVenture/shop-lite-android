package com.yashas.shoplite.presentation.home

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.yashas.shoplite.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(
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
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val currency by viewModel.currency.collectAsState()
    
    val cartItemCount = cartItems.sumOf { it.quantity }
    val cartTotal = cartItems.sumOf { it.price * it.quantity }
    
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShopLite") },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding() // Edge-to-edge support for lists below fab
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Categories
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
            
            // Sort Options
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
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
            if (!isNetworkAvailable) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You are offline. Showing cached products.",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Content
            if (state.isLoading && state.products.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!isNetworkAvailable && state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Please enable internet connection", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (state.error != null && state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }
            } else if (state.products.isEmpty() && state.searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found matching '${state.searchQuery}'")
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { 
                        isRefreshing = true
                        viewModel.refresh()
                        isRefreshing = false // Note: Actual implementation should track when loading completes
                    },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                    val coroutineScope = rememberCoroutineScope()
                    
                    LaunchedEffect(state.selectedSortOption) {
                        gridState.scrollToItem(0)
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp, 
                            end = 16.dp, 
                            top = 16.dp, 
                            bottom = 120.dp // Accommodates FAB and Navigation bar
                        ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = favoriteIds.contains(product.id),
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                ) {
                                    Text("That's all folks!", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(onClick = {
                                        coroutineScope.launch { gridState.animateScrollToItem(0) }
                                    }) {
                                        Text("Scroll up")
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

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    currency: String,
    formatPrice: (Double, String) -> String,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.LightGray
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatPrice(product.price - (product.price * (product.discountPercentage / 100)), currency),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.discountPercentage > 0) {
                            Text(
                                text = formatPrice(product.price, currency),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
