package com.insigniaempresarial.core.data.mapper

import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.database.entity.TransactionEntity
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.network.dto.TransactionDto

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
