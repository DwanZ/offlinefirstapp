package com.insigniaempresarial

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.insigniaempresarial.core.data.sync.SyncScheduler
import com.insigniaempresarial.core.domain.repository.SyncRepository
import com.insigniaempresarial.core.sync.ConnectivitySyncTrigger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class InsigniaApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var connectivitySyncTrigger: ConnectivitySyncTrigger

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        connectivitySyncTrigger.start(appScope)
        appScope.launch {
            runCatching {
                syncRepository.ensureSeeded()
                syncScheduler.enqueueSync()
            }
        }
    }
}
