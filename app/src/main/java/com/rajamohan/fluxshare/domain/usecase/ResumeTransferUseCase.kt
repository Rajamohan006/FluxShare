package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.TransferState
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ResumeTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {

    suspend operator fun invoke(transferId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            transferRepository.updateTransferState(transferId, TransferState.QUEUED)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to resume transfer")
            Result.failure(e)
        }
    }

    suspend fun getIncompleteChunks(transferId: String): List<Int> {
        return transferRepository.getIncompleteChunks(transferId).map { it.chunkIndex }
    }
}