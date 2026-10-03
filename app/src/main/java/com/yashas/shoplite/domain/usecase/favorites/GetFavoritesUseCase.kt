package com.yashas.shoplite.domain.usecase.favorites

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getFavoriteProducts()
    }
    
    fun getIds(): Flow<List<String>> {
        return repository.getFavoritesIds()
    }
}
