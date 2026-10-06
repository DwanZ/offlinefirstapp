package com.insigniaempresarial.core.domain.model

data class HomeSummary(
    val netWorthCents: Long,
    val recentTransactions: List<Transaction>,
    val pendingSyncCount: Int,
    val currency: String = "USD",
)
