package com.insigniaempresarial.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.insigniaempresarial.core.database.entity.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {
    @Query("SELECT * FROM sync_outbox ORDER BY createdAtEpochMs ASC")
    suspend fun getAllOrdered(): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE lastError IS NOT NULL")
    fun observeFailedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncOutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun delete(id: String)

    @Update
    suspend fun update(item: SyncOutboxEntity)

    @Query("UPDATE sync_outbox SET lastError = NULL, attempts = 0 WHERE lastError IS NOT NULL")
    suspend fun clearErrors()
}
