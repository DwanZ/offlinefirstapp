package com.insigniaempresarial.di

import android.content.Context
import androidx.room.Room
import com.insigniaempresarial.core.common.ConnectivityObserver
import com.insigniaempresarial.core.common.DefaultDispatcherProvider
import com.insigniaempresarial.core.common.DispatcherProvider
import com.insigniaempresarial.core.data.repository.AccountRepositoryImpl
import com.insigniaempresarial.core.data.repository.BudgetRepositoryImpl
import com.insigniaempresarial.core.data.repository.SyncRepositoryImpl
import com.insigniaempresarial.core.data.repository.TransactionRepositoryImpl
import com.insigniaempresarial.core.data.sync.SyncScheduler
import com.insigniaempresarial.core.database.InsigniaDatabase
import com.insigniaempresarial.core.domain.repository.AccountRepository
import com.insigniaempresarial.core.domain.repository.BudgetRepository
import com.insigniaempresarial.core.domain.repository.SyncRepository
import com.insigniaempresarial.core.domain.repository.TransactionRepository
import com.insigniaempresarial.core.domain.usecase.AddTransactionUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveAccountUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveAccountsUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveHomeSummaryUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveSyncHealthUseCase
import com.insigniaempresarial.core.domain.usecase.ObserveTransactionsUseCase
import com.insigniaempresarial.core.domain.usecase.RetryFailedSyncUseCase
import com.insigniaempresarial.core.domain.usecase.TransferBetweenAccountsUseCase
import com.insigniaempresarial.core.domain.usecase.TriggerSyncUseCase
import com.insigniaempresarial.core.network.NetworkFactory
import com.insigniaempresarial.core.network.api.InsigniaApi
import com.insigniaempresarial.core.sync.AndroidConnectivityObserver
import com.insigniaempresarial.core.sync.ConnectivitySyncTrigger
import com.insigniaempresarial.core.sync.WorkManagerSyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatchers(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InsigniaDatabase =
        Room.databaseBuilder(context, InsigniaDatabase::class.java, "insignia.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideApi(@ApplicationContext context: Context): InsigniaApi =
        NetworkFactory.createApi(context)

    @Provides
    @Singleton
    fun provideSyncScheduler(@ApplicationContext context: Context): SyncScheduler =
        WorkManagerSyncScheduler(context)

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver =
        AndroidConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideConnectivitySyncTrigger(
        connectivityObserver: ConnectivityObserver,
        syncScheduler: SyncScheduler,
    ): ConnectivitySyncTrigger = ConnectivitySyncTrigger(connectivityObserver, syncScheduler)

    @Provides
    @Singleton
    fun provideAccountRepository(db: InsigniaDatabase): AccountRepository =
        AccountRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideTransactionRepository(
        db: InsigniaDatabase,
        syncScheduler: SyncScheduler,
    ): TransactionRepository = TransactionRepositoryImpl(db, syncScheduler)

    @Provides
    @Singleton
    fun provideBudgetRepository(db: InsigniaDatabase): BudgetRepository =
        BudgetRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideSyncRepository(
        db: InsigniaDatabase,
        api: InsigniaApi,
        syncScheduler: SyncScheduler,
    ): SyncRepository = SyncRepositoryImpl(db, api, syncScheduler)

    @Provides fun provideObserveAccounts(repo: AccountRepository) = ObserveAccountsUseCase(repo)
    @Provides fun provideObserveAccount(repo: AccountRepository) = ObserveAccountUseCase(repo)
    @Provides fun provideObserveTransactions(repo: TransactionRepository) = ObserveTransactionsUseCase(repo)
    @Provides fun provideObserveHome(repo: SyncRepository) = ObserveHomeSummaryUseCase(repo)
    @Provides fun provideObserveSync(repo: SyncRepository) = ObserveSyncHealthUseCase(repo)
    @Provides fun provideAddTransaction(repo: TransactionRepository) = AddTransactionUseCase(repo)
    @Provides fun provideTransfer(repo: TransactionRepository) = TransferBetweenAccountsUseCase(repo)
    @Provides fun provideTriggerSync(repo: SyncRepository) = TriggerSyncUseCase(repo)
    @Provides fun provideRetryFailed(repo: SyncRepository) = RetryFailedSyncUseCase(repo)
}
