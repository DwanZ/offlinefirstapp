package com.insigniaempresarial.feature.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.usecase.ObserveSyncHealthUseCase
import com.insigniaempresarial.core.domain.usecase.RetryFailedSyncUseCase
import com.insigniaempresarial.core.domain.usecase.TriggerSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

data class SyncUiState(
    val health: SyncHealth? = null,
    val lastSyncLabel: String = "Never",
)

sealed interface SyncEffect {
    data class Message(val text: String) : SyncEffect
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    observeSyncHealth: ObserveSyncHealthUseCase,
    private val triggerSync: TriggerSyncUseCase,
    private val retryFailed: RetryFailedSyncUseCase,
) : ViewModel() {

    val state: StateFlow<SyncUiState> = observeSyncHealth()
        .map { health ->
            SyncUiState(
                health = health,
                lastSyncLabel = health.lastSuccessfulSyncAtEpochMs?.let {
                    DateFormat.getDateTimeInstance().format(Date(it))
                } ?: "Never",
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    private val _effects = MutableSharedFlow<SyncEffect>()
    val effects: SharedFlow<SyncEffect> = _effects.asSharedFlow()

    fun onSyncNow() {
        viewModelScope.launch {
            when (val result = triggerSync()) {
                is Outcome.Success -> _effects.emit(SyncEffect.Message("Sync finished"))
                is Outcome.Failure -> _effects.emit(SyncEffect.Message(result.error.message))
            }
        }
    }

    fun onRetryFailed() {
        viewModelScope.launch {
            when (val result = retryFailed()) {
                is Outcome.Success -> _effects.emit(SyncEffect.Message("Retry queued"))
                is Outcome.Failure -> _effects.emit(SyncEffect.Message(result.error.message))
            }
        }
    }
}
