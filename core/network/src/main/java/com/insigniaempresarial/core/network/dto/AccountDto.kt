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
