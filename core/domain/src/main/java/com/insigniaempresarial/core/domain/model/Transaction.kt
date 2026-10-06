package com.insigniaempresarial.core.domain.model

import com.insigniaempresarial.core.common.SyncStatus

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Long,
    val categoryId: String?,
    val note: String,
    val bookedAtEpochMs: Long,
    val syncStatus: SyncStatus,
    val remoteId: String? = null,
    val clientMutationId: String,
    val updatedAtEpochMs: Long,
)
