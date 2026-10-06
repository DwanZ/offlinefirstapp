package com.insigniaempresarial.core.domain.model

data class Budget(
    val id: String,
    val categoryId: String,
    val limitCents: Long,
    val periodStartEpochMs: Long,
    val periodEndEpochMs: Long,
)
