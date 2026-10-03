package com.yashas.shoplite.domain.usecase.wishlist

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.WishlistRepository
import javax.inject.Inject

class ToggleWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    suspend operator fun invoke(product: Product): Result<Boolean> {
        return repository.toggleWishlist(product)
    }
}
