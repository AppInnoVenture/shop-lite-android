package com.yashas.shoplite.presentation.wishlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.ui.platform.LocalLayoutDirection
import com.yashas.shoplite.presentation.wishlist.components.WishlistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    )
) {
    val state by viewModel.state.collectAsState()
    val currency by viewModel.currency.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorite products yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = padding.calculateStartPadding(layoutDirection),
                        end = padding.calculateEndPadding(layoutDirection)
                    )
            ) {
                items(state.items, key = { it.id }) { product ->
                    WishlistItem(
                        product = product,
                        currency = currency,
                        formatPrice = viewModel::formatPrice,
                        onClick = { onNavigateToProductDetails(product.id) },
                        onAddToCart = { viewModel.addToCart(product) },
                        onRemove = { viewModel.toggleFavorite(product) }
                    )
                }
            }
        }
    }
}
