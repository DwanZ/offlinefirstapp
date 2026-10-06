package com.insigniaempresarial.core.data.mapper

import com.insigniaempresarial.core.database.entity.AccountEntity
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.AccountType
import com.insigniaempresarial.core.network.dto.AccountDto

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
