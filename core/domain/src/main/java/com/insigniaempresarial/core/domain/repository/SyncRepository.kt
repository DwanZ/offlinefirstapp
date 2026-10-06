package com.insigniaempresarial.core.domain.repository

import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observeHealth(): Flow<SyncHealth>
    fun observeHomeSummary(): Flow<HomeSummary>
    suspend fun triggerSync(): Outcome<Unit>
    suspend fun retryFailed(): Outcome<Unit>
    suspend fun processSync(): Outcome<Unit>
    suspend fun ensureSeeded()
}
