package com.insigniaempresarial.core.domain.model

enum class AccountType {
    CHECKING,
    SAVINGS,
    CASH,
    CREDIT,
}

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceCents: Long,
    val updatedAtEpochMs: Long,
    val isDeleted: Boolean = false,
)

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Long,
    val categoryId: String?,
    val note: String,
    val bookedAtEpochMs: Long,
    val syncStatus: com.insigniaempresarial.core.common.SyncStatus,
    val remoteId: String? = null,
    val clientMutationId: String,
    val updatedAtEpochMs: Long,
)

data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
)

data class Budget(
    val id: String,
    val categoryId: String,
    val limitCents: Long,
    val periodStartEpochMs: Long,
    val periodEndEpochMs: Long,
)

data class SyncHealth(
    val pendingCount: Int,
    val failedCount: Int,
    val lastSuccessfulSyncAtEpochMs: Long?,
    val lastError: String?,
    val isSyncing: Boolean,
)

data class HomeSummary(
    val netWorthCents: Long,
    val recentTransactions: List<Transaction>,
    val pendingSyncCount: Int,
    val currency: String = "USD",
)
