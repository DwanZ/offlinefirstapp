package com.insigniaempresarial.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TransferBetweenAccountsUseCaseTest {

    private val repository = mockk<TransactionRepository>()
    private val useCase = TransferBetweenAccountsUseCase(repository)

    @Test
    fun `rejects non-positive amount`() = runTest {
        val zero = useCase("a", "b", 0, "note")
        val negative = useCase("a", "b", -1, "note")

        assertThat((zero as Outcome.Failure).error).isEqualTo(
            AppError.Validation(ValidationReason.TransferAmountMustBePositive),
        )
        assertThat((negative as Outcome.Failure).error).isEqualTo(
            AppError.Validation(ValidationReason.TransferAmountMustBePositive),
        )
        coVerify(exactly = 0) { repository.transfer(any(), any(), any(), any()) }
    }

    @Test
    fun `rejects same source and destination`() = runTest {
        val result = useCase("acc-1", "acc-1", 1_000, "move")

        assertThat((result as Outcome.Failure).error).isEqualTo(
            AppError.Validation(ValidationReason.AccountsMustDiffer),
        )
        coVerify(exactly = 0) { repository.transfer(any(), any(), any(), any()) }
    }

    @Test
    fun `delegates valid transfer to repository`() = runTest {
        coEvery { repository.transfer("from", "to", 2_500, "rent") } returns Outcome.Success(Unit)

        val result = useCase("from", "to", 2_500, "rent")

        assertThat(result).isEqualTo(Outcome.Success(Unit))
        coVerify(exactly = 1) { repository.transfer("from", "to", 2_500, "rent") }
    }

    @Test
    fun `propagates repository failure without wrapping message`() = runTest {
        val repoError = AppError.Validation(ValidationReason.SourceAccountNotFound)
        coEvery { repository.transfer("missing", "to", 100, "") } returns Outcome.Failure(repoError)

        val result = useCase("missing", "to", 100, "")

        assertThat((result as Outcome.Failure).error).isEqualTo(repoError)
    }
}
