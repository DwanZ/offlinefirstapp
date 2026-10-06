package com.insigniaempresarial.core.domain.repository

import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

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
