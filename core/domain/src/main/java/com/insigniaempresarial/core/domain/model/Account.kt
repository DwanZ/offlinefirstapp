package com.insigniaempresarial.core.domain.model

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceCents: Long,
    val updatedAtEpochMs: Long,
    val isDeleted: Boolean = false,
)
