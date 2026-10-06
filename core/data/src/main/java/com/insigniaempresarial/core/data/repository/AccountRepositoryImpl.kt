package com.insigniaempresarial.core.data.repository

import com.insigniaempresarial.core.data.mapper.toDomain
import com.insigniaempresarial.core.database.InsigniaDatabase
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val db: InsigniaDatabase,
) : AccountRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        db.accountDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeAccount(id: String): Flow<Account?> =
        db.accountDao().observeById(id).map { it?.toDomain() }

    override suspend fun getAccount(id: String): Account? =
        db.accountDao().getById(id)?.toDomain()
}
