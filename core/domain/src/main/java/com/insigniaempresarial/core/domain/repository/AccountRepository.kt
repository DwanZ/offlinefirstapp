package com.insigniaempresarial.core.domain.repository

import com.insigniaempresarial.core.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccount(id: String): Flow<Account?>
    suspend fun getAccount(id: String): Account?
}
