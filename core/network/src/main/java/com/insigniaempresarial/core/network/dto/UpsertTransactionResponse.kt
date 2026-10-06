package com.insigniaempresarial.core.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class UpsertTransactionResponse(
    val id: String,
    val remoteId: String,
    val clientMutationId: String,
)
