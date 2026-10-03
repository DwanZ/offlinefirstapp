package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTransactionUseCaseTest {

    private val repository = mockk<TransactionRepository>()
    private val useCase = AddTransactionUseCase(repository)

    @Test
    fun `rejects zero amount`() = runTest {
        val result = useCase("acc", 0, null, "note")
        assertTrue(result is Outcome.Failure)
        assertTrue((result as Outcome.Failure).error is AppError.Validation)
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
        assertTrue(result is Outcome.Success)
        coVerify(exactly = 1) {
            repository.addTransaction("acc", -1000, null, "coffee", 1L)
        }
    }
}
