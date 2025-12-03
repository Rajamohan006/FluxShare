package com.rajamohan.fluxshare.data.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class SHA256Verifier {

    suspend fun calculateFileHash(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val file = File(filePath)
            val buffer = ByteArray(8192)

            FileInputStream(file).use { fis ->
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }

            bytesToHex(digest.digest())
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate hash for $filePath")
            throw e
        }
    }

    suspend fun calculateDataHash(data: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(data)
            bytesToHex(digest.digest())
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate data hash")
            throw e
        }
    }

    suspend fun verifyFileHash(filePath: String, expectedHash: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val actualHash = calculateFileHash(filePath)
            actualHash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            Timber.e(e, "Hash verification failed")
            false
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}