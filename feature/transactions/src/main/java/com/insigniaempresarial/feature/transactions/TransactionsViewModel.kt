package com.insigniaempresarial.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.usecase.AddTransactionUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveAccountsUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveTransactionsUseCase
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

enum class TransactionFilter { ALL, PENDING, FAILED }

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val filter: TransactionFilter = TransactionFilter.ALL,
    val transactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val showAddSheet: Boolean = false,
)

sealed interface TransactionsEffect {
    data class Message(val text: String) : TransactionsEffect
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    observeTransactions: ObserveTransactionsUseCase,
    observeAccounts: ObserveAccountsUseCase,
    private val addTransaction: AddTransactionUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionFilter.ALL)
    private val showAdd = MutableStateFlow(false)
    private val _state = MutableStateFlow(TransactionsUiState())
    val state: StateFlow<TransactionsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TransactionsEffect>()
    val effects: SharedFlow<TransactionsEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                observeTransactions(),
                observeAccounts(),
                filter,
                showAdd,
            ) { txs, accounts, selectedFilter, addVisible ->
                val filtered = when (selectedFilter) {
                    TransactionFilter.ALL -> txs
                    TransactionFilter.PENDING -> txs.filter {
                        it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.SYNCING
                    }
                    TransactionFilter.FAILED -> txs.filter { it.syncStatus == SyncStatus.FAILED }
                }
                TransactionsUiState(
                    isLoading = false,
                    filter = selectedFilter,
                    transactions = filtered,
                    accounts = accounts,
                    showAddSheet = addVisible,
                )
            }.collect { _state.value = it }
        }
    }

    fun onFilterSelected(value: TransactionFilter) {
        filter.value = value
    }

    fun onShowAdd(show: Boolean) {
        showAdd.value = show
    }

    fun onAdd(accountId: String, amountCents: Long, note: String) {
        viewModelScope.launch {
            when (val result = addTransaction(accountId, amountCents, null, note)) {
                is Outcome.Success -> {
                    showAdd.value = false
                    _effects.emit(TransactionsEffect.Message("Saved offline — queued for sync"))
                }
                is Outcome.Failure -> _effects.emit(TransactionsEffect.Message(result.error.message))
            }
        }
    }
}
