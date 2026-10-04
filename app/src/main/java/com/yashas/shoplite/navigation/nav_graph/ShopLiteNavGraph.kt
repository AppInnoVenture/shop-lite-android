package com.yashas.shoplite.navigation.nav_graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yashas.shoplite.navigation.routes.Destination
import com.yashas.shoplite.presentation.cart.CartScreen
import com.yashas.shoplite.presentation.wishlist.WishlistScreen
import com.yashas.shoplite.presentation.catalog.CatalogScreen
import com.yashas.shoplite.presentation.itemdetails.ItemDetailsScreen
import com.yashas.shoplite.presentation.settings.SettingsScreen

@Composable
fun ShopLiteNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Destination.Main, modifier = modifier) {
        
        composable<Destination.Main> {
            CatalogScreen(
                onNavigateToProductDetails = { id -> navController.navigate(Destination.ProductDetails(id)) },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToSettings = { navController.navigate(Destination.Settings) },
                onNavigateToWishlist = { navController.navigate(Destination.Wishlist) }
            )
        }

        composable<Destination.ProductDetails> {
            ItemDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToWishlist = { navController.navigate(Destination.Wishlist) }
            )
        }

        composable<Destination.Cart> {
            CartScreen(
                onNavigateBack = { navController.popBackStack() }, 
                onNavigateToCheckout = { /* Optional */ },
                onNavigateToProductDetails = { id -> navController.navigate(Destination.ProductDetails(id)) }
            )
        }

        composable<Destination.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToWishlist = { navController.navigate(Destination.Wishlist) }
            )
        }

        composable<Destination.Wishlist> {
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetails = { id -> navController.navigate(Destination.ProductDetails(id)) }
            )
        }
    }
}
