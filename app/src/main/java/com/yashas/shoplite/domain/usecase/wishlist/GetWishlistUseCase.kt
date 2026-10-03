package com.yashas.shoplite.domain.usecase.wishlist

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getWishlistProducts()
    }
    
    fun getIds(): Flow<List<String>> {
        return repository.getWishlistIds()
    }
}
