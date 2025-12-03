package com.rajamohan.fluxshare.di

import android.content.Context
import androidx.room.Room
import com.rajamohan.fluxshare.data.local.AppDatabase
import com.rajamohan.fluxshare.data.local.ChunkDao
import com.rajamohan.fluxshare.data.local.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

// ========== Qualifiers ==========
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServerPort

// ========== Database Module ==========
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development
            .build()
    }

    @Provides
    @Singleton
    fun provideTransferDao(database: AppDatabase): TransferDao {
        return database.transferDao()
    }

    @Provides
    @Singleton
    fun provideChunkDao(database: AppDatabase): ChunkDao {
        return database.chunkDao()
    }
}