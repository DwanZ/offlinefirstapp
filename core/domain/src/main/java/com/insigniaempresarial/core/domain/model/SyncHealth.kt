package com.insigniaempresarial.core.domain.model

data class SyncHealth(
    val pendingCount: Int,
    val failedCount: Int,
    val lastSuccessfulSyncAtEpochMs: Long?,
    val lastError: String?,
    val isSyncing: Boolean,
)
