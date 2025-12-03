package com.rajamohan.fluxshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rajamohan.fluxshare.domain.model.ChunkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChunkDao {
    @Query("SELECT * FROM chunks WHERE transferId = :transferId ORDER BY chunkIndex")
    fun getChunksByTransferId(transferId: String): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE transferId = :transferId AND isCompleted = 0 ORDER BY chunkIndex")
    suspend fun getIncompleteChunks(transferId: String): List<ChunkEntity>

    @Query("SELECT * FROM chunks WHERE transferId = :transferId AND isCompleted = 1")
    suspend fun getCompletedChunks(transferId: String): List<ChunkEntity>

    @Query("SELECT COUNT(*) FROM chunks WHERE transferId = :transferId AND isCompleted = 1")
    suspend fun getCompletedChunkCount(transferId: String): Int

    @Query("SELECT * FROM chunks WHERE transferId = :transferId AND chunkIndex = :chunkIndex")
    suspend fun getChunk(transferId: String, chunkIndex: Int): ChunkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: ChunkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<ChunkEntity>)

    @Query("UPDATE chunks SET isCompleted = 1, crc32 = :crc32 WHERE transferId = :transferId AND chunkIndex = :chunkIndex")
    suspend fun markChunkComplete(transferId: String, chunkIndex: Int, crc32: Long)

    @Query("UPDATE chunks SET retryCount = retryCount + 1, lastAttempt = :timestamp WHERE transferId = :transferId AND chunkIndex = :chunkIndex")
    suspend fun incrementRetryCount(transferId: String, chunkIndex: Int, timestamp: Long)

    @Query("DELETE FROM chunks WHERE transferId = :transferId")
    suspend fun deleteChunksByTransferId(transferId: String)

    @Query("DELETE FROM chunks")
    suspend fun deleteAllChunks()
}