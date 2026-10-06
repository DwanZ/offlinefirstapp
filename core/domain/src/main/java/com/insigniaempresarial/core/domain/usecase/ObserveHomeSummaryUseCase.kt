package com.insigniaempresarial.core.domain.usecase

import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow

class ObserveHomeSummaryUseCase(
    private val repository: SyncRepository,
) {
    operator fun invoke(): Flow<HomeSummary> = repository.observeHomeSummary()
}
