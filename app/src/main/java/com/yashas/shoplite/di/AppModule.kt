package com.yashas.shoplite.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yashas.shoplite.data.local.ShopLiteDatabase
import com.yashas.shoplite.data.local.dao.CartDao
import com.yashas.shoplite.data.local.dao.FavoriteDao
import com.yashas.shoplite.data.local.dao.ProductDao
import com.yashas.shoplite.data.remote.CatalogApiService
import com.yashas.shoplite.data.util.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideApi(@ApplicationContext context: Context, gson: Gson): CatalogApiService {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        
        // 50 MB Cache
        val cacheSize = (50 * 1024 * 1024).toLong()
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
        
        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CatalogApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): ShopLiteDatabase {
        return Room.databaseBuilder(
                app,
                ShopLiteDatabase::class.java,
                "shoplite_db"
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    @Singleton
    fun provideCartDao(db: ShopLiteDatabase): CartDao = db.cartDao

    @Provides
    @Singleton
    fun provideProductDao(db: ShopLiteDatabase): ProductDao = db.productDao

    @Provides
    @Singleton
    fun provideFavoriteDao(db: ShopLiteDatabase): FavoriteDao = db.favoriteDao
    
    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(@ApplicationContext context: Context): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }
}
