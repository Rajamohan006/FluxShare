package com.rajamohan.fluxshare.data.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream

class CRC32Calculator {

    fun calculate(data: ByteArray): Long {
        val crc = java.util.zip.CRC32()
        crc.update(data)
        return crc.value
    }

    fun verify(data: ByteArray, expectedCrc: Long): Boolean {
        return calculate(data) == expectedCrc
    }

    suspend fun calculateForFile(filePath: String): Long = withContext(Dispatchers.IO) {
        try {
            val crc = java.util.zip.CRC32()
            val file = File(filePath)
            val buffer = ByteArray(8192)

            FileInputStream(file).use { fis ->
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    crc.update(buffer, 0, bytesRead)
                }
            }

            crc.value
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate CRC32 for $filePath")
            throw e
        }
    }
}