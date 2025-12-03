package com.rajamohan.fluxshare.data.security

import java.security.MessageDigest
import javax.crypto.SecretKey

class SecurityManager {

    private val sha256Verifier = SHA256Verifier()
    private val aesEncryption = AESGCMEncryption()
    private val keyExchange = KeyExchangeHandler()
    private val crc32Calculator = CRC32Calculator()

    // SHA-256 Operations
    suspend fun calculateFileHash(filePath: String): String {
        return sha256Verifier.calculateFileHash(filePath)
    }

    suspend fun verifyFileIntegrity(filePath: String, expectedHash: String): Boolean {
        return sha256Verifier.verifyFileHash(filePath, expectedHash)
    }

    // AES-GCM Operations
    fun generateEncryptionKey(): SecretKey {
        return aesEncryption.generateKey()
    }

    suspend fun encryptData(data: ByteArray, key: SecretKey): ByteArray {
        return aesEncryption.encrypt(data, key)
    }

    suspend fun decryptData(encryptedData: ByteArray, key: SecretKey): ByteArray {
        return aesEncryption.decrypt(encryptedData, key)
    }

    // Key Exchange
    fun createSessionKey(): SecretKey {
        return keyExchange.generateSessionKey()
    }

    fun exportKey(key: SecretKey): String {
        return keyExchange.createSecureHandshake(key)
    }

    fun importKey(encodedKey: String): SecretKey {
        return keyExchange.parseSecureHandshake(encodedKey)
    }

    // CRC32 Operations
    fun calculateChunkCRC(data: ByteArray): Long {
        return crc32Calculator.calculate(data)
    }

    fun verifyChunkIntegrity(data: ByteArray, expectedCrc: Long): Boolean {
        return crc32Calculator.verify(data, expectedCrc)
    }
}

// ========== Extension Functions ==========
fun ByteArray.toSHA256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this)
    return hash.joinToString("") { "%02x".format(it) }
}

fun ByteArray.toCRC32(): Long {
    val crc = java.util.zip.CRC32()
    crc.update(this)
    return crc.value
}