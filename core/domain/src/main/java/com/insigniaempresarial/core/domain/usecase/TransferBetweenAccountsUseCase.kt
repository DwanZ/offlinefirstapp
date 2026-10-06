package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.repository.TransactionRepository

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
            return Outcome.Failure(AppError.Validation(ValidationReason.TransferAmountMustBePositive))
        }
        if (fromAccountId == toAccountId) {
            return Outcome.Failure(AppError.Validation(ValidationReason.AccountsMustDiffer))
        }
        return repository.transfer(fromAccountId, toAccountId, amountCents, note)
    }
}
