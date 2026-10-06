package com.insigniaempresarial.ui

import android.content.Context
import com.insigniaempresarial.R
import com.insigniaempresarial.core.common.UserMessageKey
import com.insigniaempresarial.core.domain.message.UserMessageMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidUserMessageMapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UserMessageMapper {
    override fun map(key: UserMessageKey): String = context.getString(
        when (key) {
            UserMessageKey.SyncCompleted -> R.string.msg_sync_completed
            UserMessageKey.SyncFinished -> R.string.msg_sync_finished
            UserMessageKey.RetryQueued -> R.string.msg_retry_queued
            UserMessageKey.TransactionSavedOffline -> R.string.msg_transaction_saved_offline
            UserMessageKey.TransferSavedOffline -> R.string.msg_transfer_saved_offline
        },
    )
}
