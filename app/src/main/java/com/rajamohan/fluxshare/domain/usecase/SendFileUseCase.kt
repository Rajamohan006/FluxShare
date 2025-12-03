package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.Device
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.data.security.SecurityManager
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class SendFileUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
    private val securityManager: SecurityManager
) {

    @OptIn(InternalSerializationApi::class)
    suspend operator fun invoke(
        filePath: String,
        fileName: String,
        fileSize: Long,
        mimeType: String?,
        targetDevice: Device,
        useEncryption: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val transferId = transferRepository.createTransfer(
                fileName = fileName,
                filePath = filePath,
                fileSize = fileSize,
                mimeType = mimeType,
                direction = TransferDirection.SEND,
                peerId = targetDevice.id,
                peerName = targetDevice.name,
                isEncrypted = useEncryption
            )

            Result.success(transferId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create send transfer")
            Result.failure(e)
        }
    }

    suspend fun readChunk(filePath: String, chunkIndex: Int, chunkSize: Int): ByteArray = withContext(Dispatchers.IO) {
        val file = File(filePath)
        val offset = chunkIndex.toLong() * chunkSize
        val size = minOf(chunkSize.toLong(), file.length() - offset).toInt()

        val buffer = ByteArray(size)
        FileInputStream(file).use { fis ->
            fis.skip(offset)
            fis.read(buffer, 0, size)
        }
        buffer
    }
}