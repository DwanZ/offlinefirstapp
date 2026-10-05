package com.insigniaempresarial.core.common

import kotlinx.coroutines.flow.Flow

/**
 * Emits true when the device has validated internet connectivity.
 * Implemented in :app / :core:sync using ConnectivityManager.
 */
interface ConnectivityObserver {
    fun observeIsOnline(): Flow<Boolean>
}
