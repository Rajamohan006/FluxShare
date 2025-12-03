package com.rajamohan.fluxshare.di

import com.rajamohan.fluxshare.data.local.ChunkDao
import com.rajamohan.fluxshare.data.local.TransferDao
import com.rajamohan.fluxshare.data.network.UDPDiscoveryService
import com.rajamohan.fluxshare.data.security.SecurityManager
import com.rajamohan.fluxshare.domain.repository.DeviceRepository
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurityManager(): SecurityManager {
        return SecurityManager()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransferRepository(
        transferDao: TransferDao,
        chunkDao: ChunkDao,
        securityManager: SecurityManager
    ): TransferRepository {
        return TransferRepository(transferDao, chunkDao, securityManager)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        discoveryService: UDPDiscoveryService
    ): DeviceRepository {
        return DeviceRepository(discoveryService)
    }
}