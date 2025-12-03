package com.rajamohan.fluxshare.data.security

import javax.crypto.SecretKey

class KeyExchangeHandler {

    private val encryption = AESGCMEncryption()

    /**
     * Generate a session key for encryption
     * In production, use proper Diffie-Hellman key exchange
     */
    fun generateSessionKey(): SecretKey {
        return encryption.generateKey()
    }

    /**
     * Serialize key for secure transmission
     * In production, encrypt this with peer's public key
     */
    fun serializeKey(key: SecretKey): ByteArray {
        return encryption.keyToBytes(key)
    }

    /**
     * Deserialize received key
     */
    fun deserializeKey(keyBytes: ByteArray): SecretKey {
        return encryption.bytesToKey(keyBytes)
    }

    /**
     * Create encrypted handshake with session key
     * Returns: Base64 encoded key for transmission
     */
    fun createSecureHandshake(key: SecretKey): String {
        val keyBytes = serializeKey(key)
        return android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP)
    }

    /**
     * Parse received handshake and extract session key
     */
    fun parseSecureHandshake(encodedKey: String): SecretKey {
        val keyBytes = android.util.Base64.decode(encodedKey, android.util.Base64.NO_WRAP)
        return deserializeKey(keyBytes)
    }
}