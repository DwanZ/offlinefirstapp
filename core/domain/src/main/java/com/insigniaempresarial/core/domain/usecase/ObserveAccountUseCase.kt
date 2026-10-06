package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(id: String): Flow<Account?> = repository.observeAccount(id)
}
