package com.insigniaempresarial.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.usecase.ObserveHomeSummaryUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveSyncHealthUseCase
import com.insigniaempresarial.core.domain.usecase.TriggerSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: HomeSummary? = null,
    val syncHealth: SyncHealth? = null,
    val isRefreshing: Boolean = false,
)

sealed interface HomeUiEffect {
    data class Message(val text: String) : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeSummary: ObserveHomeSummaryUseCase,
    observeSyncHealth: ObserveSyncHealthUseCase,
    private val triggerSync: TriggerSyncUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeUiEffect>()
    val effects: SharedFlow<HomeUiEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                observeHomeSummary(),
                observeSyncHealth(),
            ) { summary, health -> summary to health }
                .collect { (summary, health) ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            syncHealth = health,
                        )
                    }
                }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = triggerSync()) {
                is Outcome.Success -> _effects.emit(HomeUiEffect.Message("Sync completed"))
                is Outcome.Failure -> _effects.emit(HomeUiEffect.Message(result.error.message))
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}
