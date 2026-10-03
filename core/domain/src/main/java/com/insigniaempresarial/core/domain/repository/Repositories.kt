package com.insigniaempresarial.core.domain.repository

import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccount(id: String): Flow<Account?>
    suspend fun getAccount(id: String): Account?
}

interface TransactionRepository {
    fun observeTransactions(accountId: String? = null): Flow<List<Transaction>>
    suspend fun addTransaction(
        accountId: String,
        amountCents: Long,
        categoryId: String?,
        note: String,
        bookedAtEpochMs: Long,
    ): Outcome<Transaction>

    suspend fun transfer(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long,
        note: String,
    ): Outcome<Unit>
}

interface BudgetRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeBudgets(): Flow<List<Budget>>
}

interface SyncRepository {
    fun observeHealth(): Flow<SyncHealth>
    fun observeHomeSummary(): Flow<HomeSummary>
    suspend fun triggerSync(): Outcome<Unit>
    suspend fun retryFailed(): Outcome<Unit>
    suspend fun processSync(): Outcome<Unit>
    suspend fun ensureSeeded()
}
