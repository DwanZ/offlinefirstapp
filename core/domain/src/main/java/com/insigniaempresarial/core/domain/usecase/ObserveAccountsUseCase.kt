package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountsUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
}
