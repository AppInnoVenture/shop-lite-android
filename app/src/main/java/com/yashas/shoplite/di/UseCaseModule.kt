package com.yashas.shoplite.di

import com.yashas.shoplite.domain.usecase.cart.AddToCartUseCase
import com.yashas.shoplite.domain.usecase.cart.CartUseCases
import com.yashas.shoplite.domain.usecase.cart.ClearCartUseCase
import com.yashas.shoplite.domain.usecase.cart.GetCartUseCase
import com.yashas.shoplite.domain.usecase.cart.RemoveFromCartUseCase
import com.yashas.shoplite.domain.usecase.cart.UpdateCartQuantityUseCase
import com.yashas.shoplite.domain.usecase.catalog.CatalogUseCases
import com.yashas.shoplite.domain.usecase.product.GetCategoriesUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductsByCategoryUseCase
import com.yashas.shoplite.domain.usecase.product.GetProductsUseCase
import com.yashas.shoplite.domain.usecase.product.SearchProductsUseCase
import com.yashas.shoplite.domain.usecase.wishlist.ClearWishlistUseCase
import com.yashas.shoplite.domain.usecase.wishlist.GetWishlistIdsUseCase
import com.yashas.shoplite.domain.usecase.wishlist.GetWishlistUseCase
import com.yashas.shoplite.domain.usecase.wishlist.ToggleWishlistUseCase
import com.yashas.shoplite.domain.usecase.wishlist.WishlistUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideCatalogUseCases(
        getProducts: GetProductsUseCase,
        searchProducts: SearchProductsUseCase,
        getCategories: GetCategoriesUseCase,
        getProductsByCategory: GetProductsByCategoryUseCase
    ): CatalogUseCases {
        return CatalogUseCases(
            getProducts = getProducts,
            searchProducts = searchProducts,
            getCategories = getCategories,
            getProductsByCategory = getProductsByCategory
        )
    }

    @Provides
    @ViewModelScoped
    fun provideCartUseCases(
        getCart: GetCartUseCase,
        addToCart: AddToCartUseCase,
        removeFromCart: RemoveFromCartUseCase,
        updateCartQuantity: UpdateCartQuantityUseCase,
        clearCart: ClearCartUseCase
    ): CartUseCases {
        return CartUseCases(
            getCart = getCart,
            addToCart = addToCart,
            removeFromCart = removeFromCart,
            updateCartQuantity = updateCartQuantity,
            clearCart = clearCart
        )
    }

    @Provides
    @ViewModelScoped
    fun provideWishlistUseCases(
        getWishlist: GetWishlistUseCase,
        toggleWishlist: ToggleWishlistUseCase,
        getWishlistIds: GetWishlistIdsUseCase,
        clearWishlist: ClearWishlistUseCase
    ): WishlistUseCases {
        return WishlistUseCases(
            getWishlist = getWishlist,
            toggleWishlist = toggleWishlist,
            getWishlistIds = getWishlistIds,
            clearWishlist = clearWishlist
        )
    }
}
