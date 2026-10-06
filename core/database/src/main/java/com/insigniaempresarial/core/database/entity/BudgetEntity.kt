package com.insigniaempresarial.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val limitCents: Long,
    val periodStartEpochMs: Long,
    val periodEndEpochMs: Long,
)
