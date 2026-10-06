package com.insigniaempresarial.feature.transactions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.common.UserMessageKey
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.message.ErrorMessageMapper
import com.insigniaempresarial.core.domain.message.UserMessageMapper
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.usecase.AddTransactionUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveAccountsUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveTransactionsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val errorMessages = object : ErrorMessageMapper {
        override fun map(error: AppError): String = when (error) {
            is AppError.Validation -> error.reason.name
            else -> "other"
        }
    }
    private val userMessages = object : UserMessageMapper {
        override fun map(key: UserMessageKey): String = when (key) {
            UserMessageKey.TransactionSavedOffline -> "saved-offline"
            else -> key.name
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAdd success closes sheet and emits mapped message`() = runTest(dispatcher) {
        val observeTx = mockk<ObserveTransactionsUseCase>()
        val observeAccounts = mockk<ObserveAccountsUseCase>()
        val add = mockk<AddTransactionUseCase>()
        every { observeTx() } returns flowOf(emptyList())
        every { observeAccounts() } returns flowOf(emptyList())
        coEvery {
            add(any(), any(), any(), any(), any())
        } returns Outcome.Success(
            Transaction(
                id = "1",
                accountId = "acc",
                amountCents = -100,
                categoryId = null,
                note = "coffee",
                bookedAtEpochMs = 1L,
                syncStatus = SyncStatus.PENDING,
                clientMutationId = "m1",
                updatedAtEpochMs = 1L,
            ),
        )

        val vm = TransactionsViewModel(observeTx, observeAccounts, add, errorMessages, userMessages)
        dispatcher.scheduler.advanceUntilIdle()
        vm.onShowAdd(true)
        dispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            vm.onAdd("acc", -100, "coffee")
            dispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(TransactionsEffect.Message("saved-offline"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.state.value.showAddSheet).isFalse()
    }

    @Test
    fun `onAdd failure emits mapped validation reason`() = runTest(dispatcher) {
        val observeTx = mockk<ObserveTransactionsUseCase>()
        val observeAccounts = mockk<ObserveAccountsUseCase>()
        val add = mockk<AddTransactionUseCase>()
        every { observeTx() } returns flowOf(emptyList())
        every { observeAccounts() } returns flowOf(emptyList())
        coEvery { add(any(), any(), any(), any(), any()) } returns Outcome.Failure(
            AppError.Validation(ValidationReason.AmountCannotBeZero),
        )

        val vm = TransactionsViewModel(observeTx, observeAccounts, add, errorMessages, userMessages)
        dispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            vm.onAdd("acc", 0, "x")
            dispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                TransactionsEffect.Message(ValidationReason.AmountCannotBeZero.name),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
