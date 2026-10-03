package com.yashas.shoplite.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yashas.shoplite.data.local.AppDatabase
import com.yashas.shoplite.data.local.CartDao
import com.yashas.shoplite.data.local.FavoriteDao
import com.yashas.shoplite.data.local.ProductDao
import com.yashas.shoplite.data.remote.DummyJsonApi
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
    fun provideApi(@ApplicationContext context: Context, gson: Gson): DummyJsonApi {
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
            .create(DummyJsonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                "shoplite_db"
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    @Singleton
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao

    @Provides
    @Singleton
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao

    @Provides
    @Singleton
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao
    
    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(@ApplicationContext context: Context): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }
}
