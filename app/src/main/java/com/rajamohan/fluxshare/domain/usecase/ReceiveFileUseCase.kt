package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.data.security.SecurityManager
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject

class ReceiveFileUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
    private val securityManager: SecurityManager
) {

    suspend operator fun invoke(
        transferId: String,
        fileName: String,
        fileSize: Long,
        mimeType: String?,
        peerId: String,
        peerName: String,
        destinationPath: String,
        sha256Hash: String,
        isEncrypted: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            transferRepository.createTransfer(
                fileName = fileName,
                filePath = destinationPath,
                fileSize = fileSize,
                mimeType = mimeType,
                direction = TransferDirection.RECEIVE,
                peerId = peerId,
                peerName = peerName,
                isEncrypted = isEncrypted
            )

            Result.success(transferId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create receive transfer")
            Result.failure(e)
        }
    }

    suspend fun writeChunk(filePath: String, chunkIndex: Int, chunkSize: Int, data: ByteArray) = withContext(Dispatchers.IO) {
        val file = File(filePath)
        file.parentFile?.mkdirs()

        val offset = chunkIndex.toLong() * chunkSize

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(offset)
            raf.write(data)
        }
    }

    suspend fun verifyFile(filePath: String, expectedHash: String): Boolean {
        return securityManager.verifyFileIntegrity(filePath, expectedHash)
    }
}