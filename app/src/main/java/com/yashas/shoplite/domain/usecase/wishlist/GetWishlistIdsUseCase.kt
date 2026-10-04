package com.yashas.shoplite.domain.usecase.wishlist

import com.yashas.shoplite.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWishlistIdsUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getWishlistIds()
    }
}
