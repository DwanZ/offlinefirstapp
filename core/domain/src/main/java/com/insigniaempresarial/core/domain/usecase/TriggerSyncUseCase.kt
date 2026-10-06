package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.domain.repository.SyncRepository

class TriggerSyncUseCase(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): Outcome<Unit> = repository.triggerSync()
}
