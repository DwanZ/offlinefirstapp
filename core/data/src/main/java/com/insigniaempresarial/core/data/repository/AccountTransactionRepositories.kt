package com.insigniaempresarial.core.data.repository

import androidx.room.withTransaction
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.Outcome
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.data.mapper.toDomain
import com.insigniaempresarial.core.data.mapper.toEntity
import com.insigniaempresarial.core.data.sync.SyncScheduler
import com.insigniaempresarial.core.database.InsigniaDatabase
import com.insigniaempresarial.core.database.entity.SyncOutboxEntity
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.Transaction
import com.insigniaempresarial.core.domain.repository.AccountRepository
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AccountRepositoryImpl(
    private val db: InsigniaDatabase,
) : AccountRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        db.accountDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeAccount(id: String): Flow<Account?> =
        db.accountDao().observeById(id).map { it?.toDomain() }

    override suspend fun getAccount(id: String): Account? =
        db.accountDao().getById(id)?.toDomain()
}

class TransactionRepositoryImpl(
    private val db: InsigniaDatabase,
    private val syncScheduler: SyncScheduler,
) : TransactionRepository {

    override fun observeTransactions(accountId: String?): Flow<List<Transaction>> {
        val flow = if (accountId == null) {
            db.transactionDao().observeAll()
        } else {
            db.transactionDao().observeByAccount(accountId)
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addTransaction(
        accountId: String,
        amountCents: Long,
        categoryId: String?,
        note: String,
        bookedAtEpochMs: Long,
    ): Outcome<Transaction> {
        return try {
            val now = System.currentTimeMillis()
            val account = db.accountDao().getById(accountId)
                ?: return Outcome.Failure(AppError.Validation("Account not found"))

            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                amountCents = amountCents,
                categoryId = categoryId,
                note = note,
                bookedAtEpochMs = bookedAtEpochMs,
                syncStatus = SyncStatus.PENDING,
                clientMutationId = UUID.randomUUID().toString(),
                updatedAtEpochMs = now,
            )
            val updatedAccount = account.copy(
                balanceCents = account.balanceCents + amountCents,
                updatedAtEpochMs = now,
            )
            val outbox = SyncOutboxEntity(
                id = UUID.randomUUID().toString(),
                entityType = "transaction",
                entityId = tx.id,
                operation = "UPSERT",
                payloadJson = buildTxnPayload(tx),
                createdAtEpochMs = now,
            )

            db.withTransaction {
                db.accountDao().upsert(updatedAccount)
                db.transactionDao().upsert(tx.toEntity())
                db.syncOutboxDao().insert(outbox)
            }
            syncScheduler.enqueueSync()
            Outcome.Success(tx)
        } catch (t: Throwable) {
            Outcome.Failure(AppError.Database(t.message ?: "Failed to save transaction"))
        }
    }

    override suspend fun transfer(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long,
        note: String,
    ): Outcome<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val from = db.accountDao().getById(fromAccountId)
                ?: return Outcome.Failure(AppError.Validation("Source account not found"))
            val to = db.accountDao().getById(toAccountId)
                ?: return Outcome.Failure(AppError.Validation("Destination account not found"))

            val debit = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = fromAccountId,
                amountCents = -amountCents,
                categoryId = "cat_transfer",
                note = note.ifBlank { "Transfer to ${to.name}" },
                bookedAtEpochMs = now,
                syncStatus = SyncStatus.PENDING,
                clientMutationId = UUID.randomUUID().toString(),
                updatedAtEpochMs = now,
            )
            val credit = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = toAccountId,
                amountCents = amountCents,
                categoryId = "cat_transfer",
                note = note.ifBlank { "Transfer from ${from.name}" },
                bookedAtEpochMs = now,
                syncStatus = SyncStatus.PENDING,
                clientMutationId = UUID.randomUUID().toString(),
                updatedAtEpochMs = now,
            )

            db.withTransaction {
                db.accountDao().upsert(
                    from.copy(balanceCents = from.balanceCents - amountCents, updatedAtEpochMs = now),
                )
                db.accountDao().upsert(
                    to.copy(balanceCents = to.balanceCents + amountCents, updatedAtEpochMs = now),
                )
                db.transactionDao().upsert(debit.toEntity())
                db.transactionDao().upsert(credit.toEntity())
                db.syncOutboxDao().insert(
                    SyncOutboxEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = "transaction",
                        entityId = debit.id,
                        operation = "UPSERT",
                        payloadJson = buildTxnPayload(debit),
                        createdAtEpochMs = now,
                    ),
                )
                db.syncOutboxDao().insert(
                    SyncOutboxEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = "transaction",
                        entityId = credit.id,
                        operation = "UPSERT",
                        payloadJson = buildTxnPayload(credit),
                        createdAtEpochMs = now,
                    ),
                )
            }
            syncScheduler.enqueueSync()
            Outcome.Success(Unit)
        } catch (t: Throwable) {
            Outcome.Failure(AppError.Database(t.message ?: "Transfer failed"))
        }
    }

    private fun buildTxnPayload(tx: Transaction): String =
        """{"clientMutationId":"${tx.clientMutationId}","accountId":"${tx.accountId}","amountCents":${tx.amountCents},"categoryId":${tx.categoryId?.let { "\"$it\"" } ?: "null"},"note":${jsonString(tx.note)},"bookedAtEpochMs":${tx.bookedAtEpochMs},"updatedAtEpochMs":${tx.updatedAtEpochMs},"localId":"${tx.id}"}"""

    private fun jsonString(value: String): String =
        "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}
