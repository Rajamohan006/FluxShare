package com.rajamohan.fluxshare.data.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESGCMEncryption {

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val secureRandom = SecureRandom()

    /**
     * Generate a new AES-256 key
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE, secureRandom)
        return keyGenerator.generateKey()
    }

    /**
     * Convert key to byte array for transmission
     */
    fun keyToBytes(key: SecretKey): ByteArray {
        return key.encoded
    }

    /**
     * Recreate key from byte array
     */
    fun bytesToKey(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, 0, keyBytes.size, ALGORITHM)
    }

    /**
     * Encrypt data with AES-GCM
     * Returns: IV (12 bytes) + Encrypted Data + Auth Tag
     */
    suspend fun encrypt(data: ByteArray, key: SecretKey): ByteArray = withContext(Dispatchers.IO) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Generate random IV
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)

            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            val encrypted = cipher.doFinal(data)

            // Prepend IV to encrypted data
            iv + encrypted
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed")
            throw e
        }
    }

    /**
     * Decrypt AES-GCM data
     * Input: IV (12 bytes) + Encrypted Data + Auth Tag
     */
    suspend fun decrypt(encryptedData: ByteArray, key: SecretKey): ByteArray = withContext(Dispatchers.IO) {
        try {
            // Extract IV
            val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed")
            throw e
        }
    }

    /**
     * Encrypt chunk data for transfer
     */
    suspend fun encryptChunk(chunkData: ByteArray, key: SecretKey): ByteArray {
        return encrypt(chunkData, key)
    }

    /**
     * Decrypt received chunk data
     */
    suspend fun decryptChunk(encryptedChunk: ByteArray, key: SecretKey): ByteArray {
        return decrypt(encryptedChunk, key)
    }
}