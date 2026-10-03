package com.insigniaempresarial.core.data.sync

interface SyncScheduler {
    fun enqueueSync(immediate: Boolean = false)
}
