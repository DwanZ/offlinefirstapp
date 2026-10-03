package com.insigniaempresarial.core.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AccountDto(
    val id: String,
    val name: String,
    val type: String,
    val currency: String,
    val balanceCents: Long,
    val updatedAtEpochMs: Long,
)

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

@JsonClass(generateAdapter = false)
data class UpsertTransactionRequest(
    val clientMutationId: String,
    val accountId: String,
    val amountCents: Long,
    val categoryId: String?,
    val note: String,
    val bookedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@JsonClass(generateAdapter = false)
data class UpsertTransactionResponse(
    val id: String,
    val remoteId: String,
    val clientMutationId: String,
)
