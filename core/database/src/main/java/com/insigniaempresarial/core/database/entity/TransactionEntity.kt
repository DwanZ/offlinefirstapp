package com.insigniaempresarial.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val amountCents: Long,
    val categoryId: String?,
    val note: String,
    val bookedAtEpochMs: Long,
    val syncStatus: String,
    val remoteId: String?,
    val clientMutationId: String,
    val updatedAtEpochMs: Long,
)
