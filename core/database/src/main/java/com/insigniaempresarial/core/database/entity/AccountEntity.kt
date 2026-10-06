package com.insigniaempresarial.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val currency: String,
    val balanceCents: Long,
    val updatedAtEpochMs: Long,
    val isDeleted: Boolean = false,
)
