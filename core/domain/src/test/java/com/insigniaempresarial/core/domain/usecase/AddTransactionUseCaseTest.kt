package com.insigniaempresarial.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddTransactionUseCaseTest {

    private val repository = mockk<TransactionRepository>()
    private val useCase = AddTransactionUseCase(repository)

    @Test
    fun `rejects zero amount with AmountCannotBeZero`() = runTest {
        val result = useCase("acc", 0, null, "note")

        assertThat(result).isInstanceOf(Outcome.Failure::class.java)
        val error = (result as Outcome.Failure).error as AppError.Validation
        assertThat(error.reason).isEqualTo(ValidationReason.AmountCannotBeZero)
        coVerify(exactly = 0) {
            repository.addTransaction(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `rejects blank account with AccountRequired`() = runTest {
        val result = useCase("  ", -500, null, "note")

        assertThat(result).isInstanceOf(Outcome.Failure::class.java)
        val error = (result as Outcome.Failure).error as AppError.Validation
        assertThat(error.reason).isEqualTo(ValidationReason.AccountRequired)
        coVerify(exactly = 0) {
            repository.addTransaction(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `delegates valid transaction to repository`() = runTest {
        val tx = Transaction(
            id = "1",
            accountId = "acc",
            amountCents = -1000,
            categoryId = null,
            note = "coffee",
            bookedAtEpochMs = 1L,
            syncStatus = SyncStatus.PENDING,
            clientMutationId = "m1",
            updatedAtEpochMs = 1L,
        )
        coEvery {
            repository.addTransaction("acc", -1000, null, "coffee", any())
        } returns Outcome.Success(tx)

        val result = useCase("acc", -1000, null, "coffee", 1L)

        assertThat(result).isEqualTo(Outcome.Success(tx))
        coVerify(exactly = 1) {
            repository.addTransaction("acc", -1000, null, "coffee", 1L)
        }
    }
}
