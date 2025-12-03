package com.rajamohan.fluxshare.domain.model

import kotlinx.serialization.InternalSerializationApi


@kotlinx.serialization.Serializable
sealed class TransferMessage {
    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class Handshake(
        val deviceId: String,
        val deviceName: String,
        val encryptionEnabled: Boolean
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class FileOffer(
        val transferId: String,
        val fileName: String,
        val fileSize: Long,
        val mimeType: String?,
        val sha256Hash: String,
        val totalChunks: Int,
        val chunkSize: Int
    ) : TransferMessage()

    @kotlinx.serialization.InternalSerializationApi @kotlinx.serialization.Serializable
    data class FileAccept(
        val transferId: String,
        val accepted: Boolean
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class ChunkRequest(
        val transferId: String,
        val chunkIndex: Int
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class ChunkData(
        val transferId: String,
        val chunkIndex: Int,
        val data: ByteArray,
        val crc32: Long
    ) : TransferMessage() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ChunkData) return false
            return transferId == other.transferId && chunkIndex == other.chunkIndex
        }

        override fun hashCode(): Int {
            return transferId.hashCode() * 31 + chunkIndex
        }
    }


    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class ChunkAck(
        val transferId: String,
        val chunkIndex: Int,
        val success: Boolean
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class TransferComplete(
        val transferId: String
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class TransferError(
        val transferId: String,
        val errorCode: String,
        val message: String
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class PauseRequest(
        val transferId: String
    ) : TransferMessage()

    @InternalSerializationApi @kotlinx.serialization.Serializable
    data class ResumeRequest(
        val transferId: String,
        val completedChunks: List<Int>
    ) : TransferMessage()
}

// ========== Settings Model ==========
data class AppSettings(
    val deviceName: String,
    val discoveryEnabled: Boolean = true,
    val encryptionEnabled: Boolean = false,
    val darkMode: Boolean = false,
    val port: Int = 8888,
    val chunkSize: Int = 1024 * 256, // 256KB
    val maxConcurrentTransfers: Int = 3,
    val autoAcceptFromKnownDevices: Boolean = false
)

// ========== Constants ==========
object TransferConstants {
    const val DEFAULT_PORT = 8888
    const val DISCOVERY_PORT = 8889
    const val CHUNK_SIZE = 256 * 1024 // 256KB
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
    const val DISCOVERY_INTERVAL_MS = 5000L
    const val PEER_TIMEOUT_MS = 30000L
    const val SOCKET_TIMEOUT_MS = 10000
    const val BUFFER_SIZE = 8192
    const val PROTOCOL_VERSION = 1
}