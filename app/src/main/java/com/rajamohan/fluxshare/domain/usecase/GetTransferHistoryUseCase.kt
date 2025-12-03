package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransferHistoryUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {

    operator fun invoke(): Flow<List<TransferEntity>> {
        return transferRepository.getAllTransfers()
    }

    fun getByDirection(direction: TransferDirection): Flow<List<TransferEntity>> {
        return transferRepository.getTransfersByDirection(direction)
    }
}