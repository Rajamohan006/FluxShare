package com.rajamohan.fluxshare.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rajamohan.fluxshare.data.local.Converters
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable


@Entity(tableName = "transfers")
@TypeConverters(Converters::class)
data class TransferEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String?,
    val direction: TransferDirection,
    val peerId: String,
    val peerName: String,
    val state: TransferState,
    val totalChunks: Int,
    val completedChunks: Int,
    val transferredBytes: Long,
    val speed: Long = 0, // bytes per second
    val startTime: Long,
    val endTime: Long? = null,
    val sha256Hash: String? = null,
    val isEncrypted: Boolean = false,
    val errorMessage: String? = null
)
enum class TransferState {
    QUEUED,
    CONNECTING,
    TRANSFERRING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    VERIFYING
}

enum class TransferDirection {
    SEND,
    RECEIVE
}
@InternalSerializationApi // ========== Network Protocol Models ==========
@Serializable
data class DiscoveryBroadcast(
    val deviceId: String,
    val deviceName: String,
    val port: Int,
    val protocolVersion: Int = 1
)