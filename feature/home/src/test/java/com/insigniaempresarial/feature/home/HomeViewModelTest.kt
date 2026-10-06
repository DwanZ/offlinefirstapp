package com.insigniaempresarial.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.UserMessageKey
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.message.ErrorMessageMapper
import com.insigniaempresarial.core.domain.message.UserMessageMapper
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.usecase.ObserveHomeSummaryUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveSyncHealthUseCase
import com.insigniaempresarial.core.domain.usecase.TriggerSyncUseCase
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
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val errorMessages = object : ErrorMessageMapper {
        override fun map(error: AppError): String = when (error) {
            is AppError.Validation -> "validation:${error.reason}"
            is AppError.Network -> "network"
            is AppError.Database -> "database"
            is AppError.Conflict -> "conflict"
            is AppError.Unknown -> "unknown"
        }
    }
    private val userMessages = object : UserMessageMapper {
        override fun map(key: UserMessageKey): String = when (key) {
            UserMessageKey.SyncCompleted -> "sync-completed"
            UserMessageKey.SyncFinished -> "sync-finished"
            UserMessageKey.RetryQueued -> "retry-queued"
            UserMessageKey.TransactionSavedOffline -> "tx-saved"
            UserMessageKey.TransferSavedOffline -> "transfer-saved"
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
    fun `maps summary and sync health into ui state`() = runTest(dispatcher) {
        val summary = HomeSummary(1_000_00, emptyList(), pendingSyncCount = 2)
        val health = SyncHealth(2, 0, 10L, null, false)
        val observeHome = mockk<ObserveHomeSummaryUseCase>()
        val observeSync = mockk<ObserveSyncHealthUseCase>()
        val trigger = mockk<TriggerSyncUseCase>()
        every { observeHome() } returns flowOf(summary)
        every { observeSync() } returns flowOf(health)
        coEvery { trigger() } returns Outcome.Success(Unit)

        val vm = HomeViewModel(observeHome, observeSync, trigger, errorMessages, userMessages)
        dispatcher.scheduler.advanceUntilIdle()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.summary).isEqualTo(summary)
            assertThat(state.syncHealth).isEqualTo(health)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefresh emits mapped success message`() = runTest(dispatcher) {
        val observeHome = mockk<ObserveHomeSummaryUseCase>()
        val observeSync = mockk<ObserveSyncHealthUseCase>()
        val trigger = mockk<TriggerSyncUseCase>()
        every { observeHome() } returns flowOf(HomeSummary(0, emptyList(), 0))
        every { observeSync() } returns flowOf(SyncHealth(0, 0, null, null, false))
        coEvery { trigger() } returns Outcome.Success(Unit)

        val vm = HomeViewModel(observeHome, observeSync, trigger, errorMessages, userMessages)
        dispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            vm.onRefresh()
            dispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(HomeUiEffect.Message("sync-completed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefresh emits mapped validation error`() = runTest(dispatcher) {
        val observeHome = mockk<ObserveHomeSummaryUseCase>()
        val observeSync = mockk<ObserveSyncHealthUseCase>()
        val trigger = mockk<TriggerSyncUseCase>()
        every { observeHome() } returns flowOf(HomeSummary(0, emptyList(), 0))
        every { observeSync() } returns flowOf(SyncHealth(0, 0, null, null, false))
        coEvery { trigger() } returns Outcome.Failure(
            AppError.Validation(ValidationReason.AccountNotFound),
        )

        val vm = HomeViewModel(observeHome, observeSync, trigger, errorMessages, userMessages)
        dispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            vm.onRefresh()
            dispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                HomeUiEffect.Message("validation:${ValidationReason.AccountNotFound}"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
