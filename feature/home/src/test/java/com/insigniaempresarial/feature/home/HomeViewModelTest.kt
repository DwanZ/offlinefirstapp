package com.insigniaempresarial.feature.home

import app.cash.turbine.test
import com.insigniaempresarial.core.common.Outcome
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `maps summary and sync health into ui state`() = runTest {
        val summary = HomeSummary(1_000_00, emptyList(), pendingSyncCount = 2)
        val health = SyncHealth(2, 0, 10L, null, false)
        val observeHome = mockk<ObserveHomeSummaryUseCase>()
        val observeSync = mockk<ObserveSyncHealthUseCase>()
        val trigger = mockk<TriggerSyncUseCase>()
        every { observeHome() } returns flowOf(summary)
        every { observeSync() } returns flowOf(health)
        coEvery { trigger() } returns Outcome.Success(Unit)

        val vm = HomeViewModel(observeHome, observeSync, trigger)
        dispatcher.scheduler.advanceUntilIdle()

        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(summary, state.summary)
            assertEquals(health, state.syncHealth)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
