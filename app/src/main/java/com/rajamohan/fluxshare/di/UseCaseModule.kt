package com.rajamohan.fluxshare.di
import com.rajamohan.fluxshare.domain.repository.DeviceRepository
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.CalculateTransferStatsUseCase
import com.rajamohan.fluxshare.domain.usecase.CancelTransferUseCase
import com.rajamohan.fluxshare.domain.usecase.DiscoverPeersUseCase
import com.rajamohan.fluxshare.domain.usecase.GetTransferHistoryUseCase
import com.rajamohan.fluxshare.domain.usecase.PauseTransferUseCase
import com.rajamohan.fluxshare.domain.usecase.ReceiveFileUseCase
import com.rajamohan.fluxshare.domain.usecase.ResumeTransferUseCase
import com.rajamohan.fluxshare.domain.usecase.SendFileUseCase
import com.rajamohan.fluxshare.data.security.SecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideDiscoverPeersUseCase(
        deviceRepository: DeviceRepository
    ): DiscoverPeersUseCase {
        return DiscoverPeersUseCase(deviceRepository)
    }

    @Provides
    @Singleton
    fun provideSendFileUseCase(
        transferRepository: TransferRepository,
        securityManager: SecurityManager
    ): SendFileUseCase {
        return SendFileUseCase(transferRepository, securityManager)
    }

    @Provides
    @Singleton
    fun provideReceiveFileUseCase(
        transferRepository: TransferRepository,
        securityManager: SecurityManager
    ): ReceiveFileUseCase {
        return ReceiveFileUseCase(transferRepository, securityManager)
    }

    @Provides
    @Singleton
    fun provideResumeTransferUseCase(
        transferRepository: TransferRepository
    ): ResumeTransferUseCase {
        return ResumeTransferUseCase(transferRepository)
    }

    @Provides
    @Singleton
    fun providePauseTransferUseCase(
        transferRepository: TransferRepository
    ): PauseTransferUseCase {
        return PauseTransferUseCase(transferRepository)
    }

    @Provides
    @Singleton
    fun provideCancelTransferUseCase(
        transferRepository: TransferRepository
    ): CancelTransferUseCase {
        return CancelTransferUseCase(transferRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransferHistoryUseCase(
        transferRepository: TransferRepository
    ): GetTransferHistoryUseCase {
        return GetTransferHistoryUseCase(transferRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateTransferStatsUseCase(): CalculateTransferStatsUseCase {
        return CalculateTransferStatsUseCase()
    }
}
