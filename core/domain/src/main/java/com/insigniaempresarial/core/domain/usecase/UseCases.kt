package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.AccountRepository
import com.insigniaempresarial.core.domain.repository.SyncRepository
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountsUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
}

class ObserveAccountUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(id: String): Flow<Account?> = repository.observeAccount(id)
}

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    operator fun invoke(accountId: String? = null): Flow<List<Transaction>> =
        repository.observeTransactions(accountId)
}

class ObserveHomeSummaryUseCase(
    private val repository: SyncRepository,
) {
    operator fun invoke(): Flow<HomeSummary> = repository.observeHomeSummary()
}

class ObserveSyncHealthUseCase(
    private val repository: SyncRepository,
) {
    operator fun invoke(): Flow<SyncHealth> = repository.observeHealth()
}

class AddTransactionUseCase(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        accountId: String,
        amountCents: Long,
        categoryId: String?,
        note: String,
        bookedAtEpochMs: Long = System.currentTimeMillis(),
    ): Outcome<Transaction> {
        if (amountCents == 0L) {
            return Outcome.Failure(
                com.insigniaempresarial.core.common.AppError.Validation("Amount cannot be zero"),
            )
        }
        if (accountId.isBlank()) {
            return Outcome.Failure(
                com.insigniaempresarial.core.common.AppError.Validation("Account is required"),
            )
        }
        return repository.addTransaction(accountId, amountCents, categoryId, note, bookedAtEpochMs)
    }
}

class TransferBetweenAccountsUseCase(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long,
        note: String = "",
    ): Outcome<Unit> {
        if (amountCents <= 0L) {
            return Outcome.Failure(
                com.insigniaempresarial.core.common.AppError.Validation("Transfer amount must be positive"),
            )
        }
        if (fromAccountId == toAccountId) {
            return Outcome.Failure(
                com.insigniaempresarial.core.common.AppError.Validation("Accounts must differ"),
            )
        }
        return repository.transfer(fromAccountId, toAccountId, amountCents, note)
    }
}

class TriggerSyncUseCase(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): Outcome<Unit> = repository.triggerSync()
}

class RetryFailedSyncUseCase(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): Outcome<Unit> = repository.retryFailed()
}
