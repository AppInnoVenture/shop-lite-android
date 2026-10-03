package com.yashas.shoplite.navigation.nav_graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yashas.shoplite.navigation.routes.Destination
import com.yashas.shoplite.presentation.cart.CartScreen
import com.yashas.shoplite.presentation.favorites.FavoritesScreen
import com.yashas.shoplite.presentation.home.HomeScreen
import com.yashas.shoplite.presentation.productdetails.ProductDetailScreen
import com.yashas.shoplite.presentation.settings.SettingsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Destination.Main, modifier = modifier) {
        
        composable<Destination.Main> {
            HomeScreen(
                onNavigateToProductDetails = { id -> navController.navigate(Destination.ProductDetails(id)) },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToSettings = { navController.navigate(Destination.Settings) },
                onNavigateToFavorites = { navController.navigate(Destination.Favorites) }
            )
        }

        composable<Destination.ProductDetails> {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToFavorites = { navController.navigate(Destination.Favorites) }
            )
        }

        composable<Destination.Cart> {
            CartScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToCheckout = { /* Optional */ })
        }

        composable<Destination.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Destination.Cart) },
                onNavigateToFavorites = { navController.navigate(Destination.Favorites) }
            )
        }

        composable<Destination.Favorites> {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetails = { id -> navController.navigate(Destination.ProductDetails(id)) }
            )
        }
    }
}
