package com.yashas.shoplite.di

import com.yashas.shoplite.data.local.AppDatabase
import com.yashas.shoplite.data.remote.DummyJsonApi
import com.yashas.shoplite.data.repository.CartRepositoryImpl
import com.yashas.shoplite.data.repository.ProductRepositoryImpl
import com.yashas.shoplite.domain.repository.CartRepository
import com.yashas.shoplite.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(api: DummyJsonApi): ProductRepository {
        return ProductRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideCartRepository(db: AppDatabase): CartRepository {
        return CartRepositoryImpl(db.cartDao)
    }
}