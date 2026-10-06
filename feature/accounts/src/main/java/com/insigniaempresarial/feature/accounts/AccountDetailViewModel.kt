package com.insigniaempresarial.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.UserMessageKey
import com.insigniaempresarial.core.domain.message.ErrorMessageMapper
import com.insigniaempresarial.core.domain.message.UserMessageMapper
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.BudgetRepository
import com.insigniaempresarial.core.domain.usecase.ObserveAccountUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveAccountsUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveTransactionsUseCase
import com.insigniaempresarial.core.domain.usecase.TransferBetweenAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val ledger: List<Transaction> = emptyList(),
    val allAccounts: List<Account> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showTransfer: Boolean = false,
)

sealed interface AccountDetailEffect {
    data class Message(val text: String) : AccountDetailEffect
}

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAccount: ObserveAccountUseCase,
    observeTransactions: ObserveTransactionsUseCase,
    observeAccounts: ObserveAccountsUseCase,
    budgetRepository: BudgetRepository,
    private val transfer: TransferBetweenAccountsUseCase,
    private val errorMessages: ErrorMessageMapper,
    private val userMessages: UserMessageMapper,
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle["accountId"])

    private val showTransfer = MutableStateFlow(false)
    private val _state = MutableStateFlow(AccountDetailUiState())
    val state: StateFlow<AccountDetailUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AccountDetailEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: SharedFlow<AccountDetailEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAccount(accountId),
                observeTransactions(accountId),
                observeAccounts(),
                budgetRepository.observeBudgets(),
                budgetRepository.observeCategories(),
            ) { account, ledger, accounts, budgets, categories ->
                AccountDetailUiState(
                    isLoading = false,
                    account = account,
                    ledger = ledger.take(20),
                    allAccounts = accounts,
                    budgets = budgets,
                    categories = categories,
                    showTransfer = false,
                )
            }.combine(showTransfer) { base, transferVisible ->
                base.copy(showTransfer = transferVisible)
            }.collect { _state.value = it }
        }
    }

    fun onShowTransfer(show: Boolean) {
        showTransfer.value = show
    }

    fun onTransfer(toAccountId: String, amountCents: Long, note: String) {
        viewModelScope.launch {
            when (
                val result = transfer(
                    fromAccountId = accountId,
                    toAccountId = toAccountId,
                    amountCents = amountCents,
                    note = note,
                )
            ) {
                is Outcome.Success -> {
                    showTransfer.value = false
                    _effects.emit(
                        AccountDetailEffect.Message(
                            userMessages.map(UserMessageKey.TransferSavedOffline),
                        ),
                    )
                }
                is Outcome.Failure -> _effects.emit(
                    AccountDetailEffect.Message(errorMessages.map(result.error)),
                )
            }
        }
    }
}
