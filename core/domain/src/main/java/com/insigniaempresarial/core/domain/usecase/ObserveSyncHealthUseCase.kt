package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow

class ObserveSyncHealthUseCase(
    private val repository: SyncRepository,
) {
    operator fun invoke(): Flow<SyncHealth> = repository.observeHealth()
}
