package com.yashas.shoplite.domain.usecase.product

import com.yashas.shoplite.domain.repository.ProductRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.getCategories()
    }
}
