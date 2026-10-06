package com.insigniaempresarial.core.sync

import com.insigniaempresarial.core.common.ConnectivityObserver
import com.insigniaempresarial.core.data.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectivitySyncTrigger(
    private val connectivityObserver: ConnectivityObserver,
    private val syncScheduler: SyncScheduler,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            var wasOffline = false
            connectivityObserver.observeIsOnline().collect { online ->
                if (online && wasOffline) {
                    syncScheduler.enqueueSync(immediate = true)
                }
                wasOffline = !online
            }
        }
    }
}
