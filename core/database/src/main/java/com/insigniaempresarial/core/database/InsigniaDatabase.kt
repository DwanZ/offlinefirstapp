package com.insigniaempresarial.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.insigniaempresarial.core.database.dao.AccountDao
import com.insigniaempresarial.core.database.dao.BudgetDao
import com.insigniaempresarial.core.database.dao.CategoryDao
import com.insigniaempresarial.core.database.dao.SyncMetaDao
import com.insigniaempresarial.core.database.dao.SyncOutboxDao
import com.insigniaempresarial.core.database.dao.TransactionDao
import com.insigniaempresarial.core.database.entity.AccountEntity
import com.insigniaempresarial.core.database.entity.BudgetEntity
import com.insigniaempresarial.core.database.entity.CategoryEntity
import com.insigniaempresarial.core.database.entity.SyncMetaEntity
import com.insigniaempresarial.core.database.entity.SyncOutboxEntity
import com.insigniaempresarial.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SyncOutboxEntity::class,
        SyncMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class InsigniaDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun syncOutboxDao(): SyncOutboxDao
    abstract fun syncMetaDao(): SyncMetaDao
}
