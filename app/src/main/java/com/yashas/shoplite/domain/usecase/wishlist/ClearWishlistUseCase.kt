package com.yashas.shoplite.domain.usecase.wishlist

import com.yashas.shoplite.domain.repository.WishlistRepository
import javax.inject.Inject

class ClearWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.clearWishlist()
    }
}
