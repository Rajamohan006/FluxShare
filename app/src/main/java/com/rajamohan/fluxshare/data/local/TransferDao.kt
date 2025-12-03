package com.rajamohan.fluxshare.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferState
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY startTime DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE id = :transferId")
    fun getTransferById(transferId: String): Flow<TransferEntity?>

    @Query("SELECT * FROM transfers WHERE state = :state")
    fun getTransfersByState(state: TransferState): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE state IN (:states)")
    fun getActiveTransfers(states: List<TransferState> = listOf(
        TransferState.QUEUED,
        TransferState.CONNECTING,
        TransferState.TRANSFERRING,
        TransferState.PAUSED,
        TransferState.VERIFYING
    )): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE direction = :direction ORDER BY startTime DESC")
    fun getTransfersByDirection(direction: TransferDirection): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Update
    suspend fun updateTransfer(transfer: TransferEntity)

    @Query("UPDATE transfers SET state = :state WHERE id = :transferId")
    suspend fun updateTransferState(transferId: String, state: TransferState)

    @Query("UPDATE transfers SET completedChunks = :completedChunks, transferredBytes = :transferredBytes, speed = :speed WHERE id = :transferId")
    suspend fun updateProgress(transferId: String, completedChunks: Int, transferredBytes: Long, speed: Long)

    @Query("UPDATE transfers SET state = :state, endTime = :endTime, sha256Hash = :hash WHERE id = :transferId")
    suspend fun completeTransfer(transferId: String, state: TransferState, endTime: Long, hash: String?)

    @Query("UPDATE transfers SET state = :state, errorMessage = :error WHERE id = :transferId")
    suspend fun failTransfer(transferId: String, state: TransferState, error: String)

    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers WHERE id = :transferId")
    suspend fun deleteTransferById(transferId: String)

    @Query("DELETE FROM transfers WHERE state IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun clearCompletedTransfers()

    @Query("DELETE FROM transfers")
    suspend fun deleteAllTransfers()
}