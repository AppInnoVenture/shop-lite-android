package com.yashas.shoplite.presentation.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coil.compose.AsyncImage
import com.yashas.shoplite.domain.model.Review

import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Close
import com.yashas.shoplite.presentation.components.AnimatedIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    viewModel: ItemDetailsViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    )
) {
    val state by viewModel.state.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var zoomedImage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isFavorite = state.product?.id?.let { favoriteIds.contains(it) } == true
                    AnimatedIconButton(onClick = { state.product?.let { viewModel.toggleFavorite(it) } }) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
        },
        bottomBar = {
            state.product?.let { product ->
                val cartItem = cartItems.find { it.productId == product.id }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (cartItem != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                AnimatedIconButton(
                                    onClick = { viewModel.updateCartQuantity(cartItem.productId, cartItem.quantity - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleLarge)
                                }
                                Text(
                                    text = cartItem.quantity.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                AnimatedIconButton(
                                    onClick = { viewModel.updateCartQuantity(cartItem.productId, cartItem.quantity + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleLarge)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = onNavigateToCart,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Cart")
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.addToCart(product) },
                                modifier = Modifier.weight(1f),
                                enabled = product.stock > 0
                            ) {
                                Text(if (product.stock > 0) "Add to Cart" else "Out of Stock")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        } else if (state.product != null) {
            val product = state.product!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Image Carousel
                item {
                    val images = if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl)
                    val pagerState = rememberPagerState(pageCount = { images.size })
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) { page ->
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(images[page])
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color.LightGray),
                                error = androidx.compose.ui.res.painterResource(com.yashas.shoplite.R.drawable.baseline_image_not_available_24),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { zoomedImage = images[page] },
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        // Dots indicator
                        if (images.size > 1) {
                            Row(
                                Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(images.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Transparent
                                    val borderColor = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Gray
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, borderColor, CircleShape)
                                            .size(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = viewModel.formatPrice(product.price - (product.price * (product.discountPercentage / 100)), currency),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (product.discountPercentage > 0) {
                                    Text(
                                        text = viewModel.formatPrice(product.price, currency),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ),
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = product.rating.toString(), style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Brand: ${product.brand}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Category: ${product.category.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
                        if (product.stock < 10) {
                            Text(text = "Hurry, only ${product.stock} left in stock!", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                        } else {
                            Text(text = "In Stock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(product.description, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Shipping & Warranty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Warranty: ${product.warrantyInformation}", style = MaterialTheme.typography.bodyMedium)
                        Text("Shipping: ${product.shippingInformation}", style = MaterialTheme.typography.bodyMedium)
                        Text("Return Policy: ${product.returnPolicy}", style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Dimensions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("W: ${product.dimensions.width} x H: ${product.dimensions.height} x D: ${product.dimensions.depth}", style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                if (product.reviews.isEmpty()) {
                    item {
                        Text(
                            "No reviews yet.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(product.reviews) { review ->
                        ReviewItem(review)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    if (zoomedImage != null) {
        Dialog(
            onDismissRequest = { zoomedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                AsyncImage(
                    model = zoomedImage,
                    contentDescription = "Zoomed Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offsetX += pan.x * scale
                                    offsetY += pan.y * scale
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = { zoomedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = review.date.substringBefore("T"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
