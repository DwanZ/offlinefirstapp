package com.insigniaempresarial.core.network.dto

import com.squareup.moshi.JsonClass

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
