package com.yashas.shoplite.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.yashas.shoplite.presentation.cart.components.CartSummary
import androidx.compose.ui.platform.LocalLayoutDirection
import com.yashas.shoplite.presentation.cart.components.CartItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    viewModel: CartViewModel = hiltViewModel(
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
                title = { Text("My Cart") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.items.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                CartSummary(
                    subtotal = state.subtotal,
                    deliveryFee = state.deliveryFee,
                    total = state.total,
                    currency = currency,
                    formatPrice = viewModel::formatPrice,
                    onCheckout = onNavigateToCheckout
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
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
                items(state.items, key = { it.productId }) { item ->
                    CartItemRow(
                        item = item,
                        currency = currency,
                        formatPrice = viewModel::formatPrice,
                        onClick = { onNavigateToProductDetails(item.productId) },
                        onQuantityChanged = { newQty -> viewModel.updateQuantity(item.productId, newQty) },
                        onRemove = { viewModel.removeItem(item.productId) }
                    )
                }
            }
        }
    }
}
