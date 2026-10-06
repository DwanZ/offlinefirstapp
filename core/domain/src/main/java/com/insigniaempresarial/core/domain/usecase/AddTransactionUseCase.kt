package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.TransactionRepository

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
            return Outcome.Failure(AppError.Validation(ValidationReason.AmountCannotBeZero))
        }
        if (accountId.isBlank()) {
            return Outcome.Failure(AppError.Validation(ValidationReason.AccountRequired))
        }
        return repository.addTransaction(accountId, amountCents, categoryId, note, bookedAtEpochMs)
    }
}
