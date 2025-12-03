package com.rajamohan.fluxshare.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rajamohan.fluxshare.domain.model.ChunkEntity
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferState
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [TransferEntity::class, ChunkEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao
    abstract fun chunkDao(): ChunkDao

    companion object {
        const val DATABASE_NAME = "fluxshare_database"
    }
}
// ========== Database Migrations (for future use) ==========
object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add a new column in future versions
            // database.execSQL("ALTER TABLE transfers ADD COLUMN newColumn TEXT")
        }
    }
}

// ========== Data class for Transfer with related data ==========
data class TransferWithChunksData(
    @Embedded val transfer: TransferEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transferId"
    )
    val chunks: List<ChunkEntity>
)

// ========== Extended DAO with Relations ==========
@Dao
interface TransferWithChunksDao {
    @Transaction
    @Query("SELECT * FROM transfers WHERE id = :transferId")
    fun getTransferWithChunks(transferId: String): Flow<TransferWithChunksData?>

    @Transaction
    @Query("SELECT * FROM transfers WHERE state IN (:states) ORDER BY startTime DESC")
    fun getActiveTransfersWithChunks(
        states: List<TransferState> = listOf(
            TransferState.QUEUED,
            TransferState.CONNECTING,
            TransferState.TRANSFERRING,
            TransferState.PAUSED,
            TransferState.VERIFYING
        )
    ): Flow<List<TransferWithChunksData>>
}