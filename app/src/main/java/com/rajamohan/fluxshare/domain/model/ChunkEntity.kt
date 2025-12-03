package com.rajamohan.fluxshare.domain.model

import androidx.room.Entity

@Entity(
    tableName = "chunks",
    primaryKeys = ["transferId", "chunkIndex"]
)
data class ChunkEntity(
    val transferId: String,
    val chunkIndex: Int,
    val chunkSize: Int,
    val isCompleted: Boolean = false,
    val crc32: Long? = null,
    val retryCount: Int = 0,
    val lastAttempt: Long? = null
)
data class TransferStats(
    val currentSpeed: Long, // bytes/sec
    val averageSpeed: Long, // bytes/sec
    val estimatedTimeRemaining: Long, // milliseconds
    val progressPercentage: Float // 0.0 to 1.0
)
// ========== Transfer with Chunks (for UI) ==========
data class TransferWithChunks(
    val transfer: TransferEntity,
    val chunks: List<ChunkEntity>,
    val stats: TransferStats
)
