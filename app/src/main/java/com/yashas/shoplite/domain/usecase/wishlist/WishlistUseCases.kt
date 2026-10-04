package com.yashas.shoplite.domain.usecase.wishlist

data class WishlistUseCases(
    val getWishlist: GetWishlistUseCase,
    val toggleWishlist: ToggleWishlistUseCase,
    val getWishlistIds: GetWishlistIdsUseCase
)
