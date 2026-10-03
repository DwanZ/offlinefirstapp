package com.insigniaempresarial.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.data.sync.SyncScheduler
import com.insigniaempresarial.core.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return when (val outcome = syncRepository.processSync()) {
            is Outcome.Success -> Result.success()
            is Outcome.Failure -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}

class WorkManagerSyncScheduler(
    private val context: Context,
) : SyncScheduler {

    override fun enqueueSync(immediate: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        val policy = if (immediate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_NAME, policy, request)
    }

    companion object {
        const val UNIQUE_NAME = "insignia_sync"
    }
}
