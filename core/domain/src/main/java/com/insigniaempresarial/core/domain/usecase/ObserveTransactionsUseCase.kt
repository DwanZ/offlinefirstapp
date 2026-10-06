package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    operator fun invoke(accountId: String? = null): Flow<List<Transaction>> =
        repository.observeTransactions(accountId)
}
