package com.rajamohan.fluxshare.domain.repository

import com.rajamohan.fluxshare.data.local.ChunkDao
import com.rajamohan.fluxshare.data.local.TransferDao
import com.rajamohan.fluxshare.domain.model.TransferConstants
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferState
import com.rajamohan.fluxshare.data.security.SecurityManager
import com.rajamohan.fluxshare.domain.model.ChunkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val transferDao: TransferDao,
    private val chunkDao: ChunkDao,
    private val securityManager: SecurityManager
) {

    fun getAllTransfers(): Flow<List<TransferEntity>> {
        return transferDao.getAllTransfers()
    }

    fun getTransferById(transferId: String): Flow<TransferEntity?> {
        return transferDao.getTransferById(transferId)
    }

    fun getActiveTransfers(): Flow<List<TransferEntity>> {
        return transferDao.getActiveTransfers()
    }

    fun getTransfersByDirection(direction: TransferDirection): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByDirection(direction)
    }

    suspend fun createTransfer(
        fileName: String,
        filePath: String,
        fileSize: Long,
        mimeType: String?,
        direction: TransferDirection,
        peerId: String,
        peerName: String,
        isEncrypted: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val transferId = UUID.randomUUID().toString()
        val chunkSize = TransferConstants.CHUNK_SIZE
        val totalChunks = ((fileSize + chunkSize - 1) / chunkSize).toInt()

        // Calculate SHA-256 hash for sender
        val sha256Hash = if (direction == TransferDirection.SEND) {
            securityManager.calculateFileHash(filePath)
        } else null

        val transfer = TransferEntity(
            id = transferId,
            fileName = fileName,
            filePath = filePath,
            fileSize = fileSize,
            mimeType = mimeType,
            direction = direction,
            peerId = peerId,
            peerName = peerName,
            state = TransferState.QUEUED,
            totalChunks = totalChunks,
            completedChunks = 0,
            transferredBytes = 0,
            startTime = System.currentTimeMillis(),
            sha256Hash = sha256Hash,
            isEncrypted = isEncrypted
        )

        transferDao.insertTransfer(transfer)

        // Create chunk records
        val chunks = (0 until totalChunks).map { index ->
            ChunkEntity(
                transferId = transferId,
                chunkIndex = index,
                chunkSize = chunkSize,
                isCompleted = false
            )
        }
        chunkDao.insertChunks(chunks)

        transferId
    }

    suspend fun updateTransferState(transferId: String, state: TransferState) {
        transferDao.updateTransferState(transferId, state)
    }

    suspend fun updateProgress(transferId: String, completedChunks: Int, transferredBytes: Long, speed: Long) {
        transferDao.updateProgress(transferId, completedChunks, transferredBytes, speed)
    }

    suspend fun completeTransfer(transferId: String, hash: String? = null) {
        transferDao.completeTransfer(
            transferId = transferId,
            state = TransferState.COMPLETED,
            endTime = System.currentTimeMillis(),
            hash = hash
        )
    }

    suspend fun failTransfer(transferId: String, error: String) {
        transferDao.failTransfer(transferId, TransferState.FAILED, error)
    }

    suspend fun deleteTransfer(transferId: String) {
        chunkDao.deleteChunksByTransferId(transferId)
        transferDao.deleteTransferById(transferId)
    }

    suspend fun clearCompletedTransfers() {
        transferDao.clearCompletedTransfers()
    }

    // Chunk operations
    fun getChunks(transferId: String): Flow<List<ChunkEntity>> {
        return chunkDao.getChunksByTransferId(transferId)
    }

    suspend fun getIncompleteChunks(transferId: String): List<ChunkEntity> {
        return chunkDao.getIncompleteChunks(transferId)
    }

    suspend fun markChunkComplete(transferId: String, chunkIndex: Int, crc32: Long) {
        chunkDao.markChunkComplete(transferId, chunkIndex, crc32)
    }

    suspend fun getCompletedChunkCount(transferId: String): Int {
        return chunkDao.getCompletedChunkCount(transferId)
    }
}