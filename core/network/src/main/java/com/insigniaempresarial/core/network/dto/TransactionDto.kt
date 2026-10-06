package com.insigniaempresarial.core.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class TransactionDto(
    val id: String,
    val accountId: String,
    val amountCents: Long,
    val categoryId: String?,
    val note: String,
    val bookedAtEpochMs: Long,
    val remoteId: String?,
    val clientMutationId: String,
    val updatedAtEpochMs: Long,
)
