package com.insigniaempresarial.core.data.repository

import androidx.room.withTransaction
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.data.mapper.toDomain
import com.insigniaempresarial.core.data.mapper.toEntity
import com.insigniaempresarial.core.data.sync.SyncScheduler
import com.insigniaempresarial.core.database.InsigniaDatabase
import com.insigniaempresarial.core.database.entity.BudgetEntity
import com.insigniaempresarial.core.database.entity.CategoryEntity
import com.insigniaempresarial.core.database.entity.SyncMetaEntity
import com.insigniaempresarial.core.domain.model.HomeSummary
import com.insigniaempresarial.core.domain.model.SyncHealth
import com.insigniaempresarial.core.domain.repository.SyncRepository
import com.insigniaempresarial.core.network.api.InsigniaApi
import com.insigniaempresarial.core.network.dto.UpsertTransactionRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.IOException

class SyncRepositoryImpl(
    private val db: InsigniaDatabase,
    private val api: InsigniaApi,
    private val syncScheduler: SyncScheduler,
) : SyncRepository {

    private val syncing = MutableStateFlow(false)
    private val lastError = MutableStateFlow<String?>(null)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val payloadAdapter = moshi.adapter(OutboxTxnPayload::class.java)

    override fun observeHealth(): Flow<SyncHealth> =
        combine(
            db.syncOutboxDao().observeCount(),
            db.syncOutboxDao().observeFailedCount(),
            db.syncMetaDao().observe(KEY_LAST_SYNC),
            syncing,
            lastError,
        ) { pending, failed, meta, isSyncing, error ->
            SyncHealth(
                pendingCount = pending,
                failedCount = failed,
                lastSuccessfulSyncAtEpochMs = meta?.value?.toLongOrNull(),
                lastError = error,
                isSyncing = isSyncing,
            )
        }

    override fun observeHomeSummary(): Flow<HomeSummary> =
        combine(
            db.accountDao().observeAll(),
            db.transactionDao().observeAll(),
            db.syncOutboxDao().observeCount(),
        ) { accounts, transactions, pending ->
            HomeSummary(
                netWorthCents = accounts.sumOf { it.balanceCents },
                recentTransactions = transactions.take(8).map { it.toDomain() },
                pendingSyncCount = pending,
                currency = accounts.firstOrNull()?.currency ?: "USD",
            )
        }

    override suspend fun triggerSync(): Outcome<Unit> {
        syncScheduler.enqueueSync(immediate = true)
        return processSync()
    }

    override suspend fun retryFailed(): Outcome<Unit> {
        db.syncOutboxDao().clearErrors()
        val failed = db.transactionDao().getByStatus(SyncStatus.FAILED.name)
        failed.forEach { tx ->
            db.transactionDao().update(tx.copy(syncStatus = SyncStatus.PENDING.name))
        }
        return triggerSync()
    }

    override suspend fun processSync(): Outcome<Unit> {
        if (!syncing.compareAndSet(expect = false, update = true)) {
            return Outcome.Success(Unit)
        }
        return try {
            pullRemote()
            pushOutbox()
            db.syncMetaDao().upsert(
                SyncMetaEntity(KEY_LAST_SYNC, System.currentTimeMillis().toString()),
            )
            lastError.value = null
            Outcome.Success(Unit)
        } catch (t: Throwable) {
            lastError.value = t.message
            Outcome.Failure(
                if (t is IOException) {
                    AppError.Network(detail = t.message)
                } else {
                    AppError.Unknown(detail = t.message, cause = t)
                },
            )
        } finally {
            syncing.value = false
        }
    }

    override suspend fun ensureSeeded() {
        if (db.accountDao().count() > 0) return
        val now = System.currentTimeMillis()
        val accounts = api.getAccounts()
        val transactions = api.getTransactions()
        db.withTransaction {
            db.accountDao().upsertAll(accounts.map { it.toEntity() })
            db.transactionDao().upsertAll(transactions.map { it.toEntity(SyncStatus.SYNCED) })
            db.categoryDao().upsertAll(
                listOf(
                    CategoryEntity("cat_ops", "Operations", "build"),
                    CategoryEntity("cat_revenue", "Revenue", "trending_up"),
                    CategoryEntity("cat_transfer", "Transfers", "swap_horiz"),
                    CategoryEntity("cat_payroll", "Payroll", "payments"),
                ),
            )
            db.budgetDao().upsertAll(
                listOf(
                    BudgetEntity(
                        id = "bud_ops",
                        categoryId = "cat_ops",
                        limitCents = 200000,
                        periodStartEpochMs = now - 15L * 24 * 60 * 60 * 1000,
                        periodEndEpochMs = now + 15L * 24 * 60 * 60 * 1000,
                    ),
                ),
            )
            db.syncMetaDao().upsert(SyncMetaEntity(KEY_LAST_SYNC, now.toString()))
        }
    }

    private suspend fun pullRemote() {
        val since = db.syncMetaDao().get(KEY_LAST_SYNC)?.value?.toLongOrNull()
        val remoteAccounts = api.getAccounts(since)
        val remoteTx = api.getTransactions(since)

        db.withTransaction {
            remoteAccounts.forEach { dto ->
                val local = db.accountDao().getById(dto.id)
                if (local == null || dto.updatedAtEpochMs >= local.updatedAtEpochMs) {
                    db.accountDao().upsert(dto.toEntity())
                }
            }
            remoteTx.forEach { dto ->
                val local = db.transactionDao().getById(dto.id)
                if (local == null || dto.updatedAtEpochMs >= local.updatedAtEpochMs) {
                    db.transactionDao().upsert(dto.toEntity(SyncStatus.SYNCED))
                }
            }
        }
    }

    private suspend fun pushOutbox() {
        val items = db.syncOutboxDao().getAllOrdered()
        for (item in items) {
            if (item.entityType != "transaction") {
                db.syncOutboxDao().delete(item.id)
                continue
            }
            val payload = payloadAdapter.fromJson(item.payloadJson)
                ?: run {
                    db.syncOutboxDao().update(
                        item.copy(attempts = item.attempts + 1, lastError = "INVALID_PAYLOAD"),
                    )
                    continue
                }
            val local = db.transactionDao().getById(item.entityId)
            if (local != null) {
                db.transactionDao().update(local.copy(syncStatus = SyncStatus.SYNCING.name))
            }
            try {
                val response = api.upsertTransaction(
                    UpsertTransactionRequest(
                        clientMutationId = payload.clientMutationId,
                        accountId = payload.accountId,
                        amountCents = payload.amountCents,
                        categoryId = payload.categoryId,
                        note = payload.note,
                        bookedAtEpochMs = payload.bookedAtEpochMs,
                        updatedAtEpochMs = payload.updatedAtEpochMs,
                    ),
                )
                db.withTransaction {
                    if (local != null) {
                        db.transactionDao().update(
                            local.copy(
                                syncStatus = SyncStatus.SYNCED.name,
                                remoteId = response.remoteId,
                            ),
                        )
                    }
                    db.syncOutboxDao().delete(item.id)
                }
            } catch (t: Throwable) {
                val detail = t.message ?: t::class.java.simpleName
                if (local != null) {
                    db.transactionDao().update(local.copy(syncStatus = SyncStatus.FAILED.name))
                }
                db.syncOutboxDao().update(
                    item.copy(attempts = item.attempts + 1, lastError = detail),
                )
                // Non-IO failures stay FAILED; IO bubbles for WorkManager retry
                if (t is IOException) throw t
            }
        }
    }

    private data class OutboxTxnPayload(
        val clientMutationId: String,
        val accountId: String,
        val amountCents: Long,
        val categoryId: String?,
        val note: String,
        val bookedAtEpochMs: Long,
        val updatedAtEpochMs: Long,
        val localId: String,
    )

    companion object {
        const val KEY_LAST_SYNC = "last_successful_sync_at"
    }
}
