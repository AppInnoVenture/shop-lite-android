package com.yashas.shoplite.domain.usecase.favorites

import com.yashas.shoplite.domain.model.Product
import com.yashas.shoplite.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(product: Product): Result<Boolean> {
        return repository.toggleFavorite(product)
    }
}
