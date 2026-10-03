package com.insigniaempresarial.core.data.mapper

import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.database.entity.AccountEntity
import com.insigniaempresarial.core.database.entity.BudgetEntity
import com.insigniaempresarial.core.database.entity.CategoryEntity
import com.insigniaempresarial.core.database.entity.TransactionEntity
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.AccountType
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.network.dto.AccountDto
import com.insigniaempresarial.core.network.dto.TransactionDto

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = runCatching { AccountType.valueOf(type) }.getOrDefault(AccountType.CHECKING),
    currency = currency,
    balanceCents = balanceCents,
    updatedAtEpochMs = updatedAtEpochMs,
    isDeleted = isDeleted,
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type.name,
    currency = currency,
    balanceCents = balanceCents,
    updatedAtEpochMs = updatedAtEpochMs,
    isDeleted = isDeleted,
)

fun AccountDto.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type,
    currency = currency,
    balanceCents = balanceCents,
    updatedAtEpochMs = updatedAtEpochMs,
    isDeleted = false,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    amountCents = amountCents,
    categoryId = categoryId,
    note = note,
    bookedAtEpochMs = bookedAtEpochMs,
    syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.SYNCED),
    remoteId = remoteId,
    clientMutationId = clientMutationId,
    updatedAtEpochMs = updatedAtEpochMs,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    accountId = accountId,
    amountCents = amountCents,
    categoryId = categoryId,
    note = note,
    bookedAtEpochMs = bookedAtEpochMs,
    syncStatus = syncStatus.name,
    remoteId = remoteId,
    clientMutationId = clientMutationId,
    updatedAtEpochMs = updatedAtEpochMs,
)

fun TransactionDto.toEntity(status: SyncStatus = SyncStatus.SYNCED) = TransactionEntity(
    id = id,
    accountId = accountId,
    amountCents = amountCents,
    categoryId = categoryId,
    note = note,
    bookedAtEpochMs = bookedAtEpochMs,
    syncStatus = status.name,
    remoteId = remoteId,
    clientMutationId = clientMutationId,
    updatedAtEpochMs = updatedAtEpochMs,
)

fun CategoryEntity.toDomain() = Category(id = id, name = name, iconKey = iconKey)
fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limitCents = limitCents,
    periodStartEpochMs = periodStartEpochMs,
    periodEndEpochMs = periodEndEpochMs,
)
