package com.yashas.shoplite.di

import com.yashas.shoplite.data.repository.CartRepositoryImpl
import com.yashas.shoplite.data.repository.FavoritesRepositoryImpl
import com.yashas.shoplite.data.repository.ProductRepositoryImpl
import com.yashas.shoplite.data.repository.SettingsRepositoryImpl
import com.yashas.shoplite.domain.repository.CartRepository
import com.yashas.shoplite.domain.repository.FavoritesRepository
import com.yashas.shoplite.domain.repository.ProductRepository
import com.yashas.shoplite.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
    
    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        favoritesRepositoryImpl: FavoritesRepositoryImpl
    ): FavoritesRepository
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
