package com.insigniaempresarial.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.designsystem.component.MetricCard
import com.insigniaempresarial.core.designsystem.component.MoneyText
import com.insigniaempresarial.core.designsystem.component.SyncStatusChip
import com.insigniaempresarial.core.designsystem.component.SyncTone
import com.insigniaempresarial.core.designsystem.component.formatMoney
import com.insigniaempresarial.core.domain.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onMessage: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.Message -> onMessage(effect.text)
            }
        }
    }
    HomeScreen(
        state = state,
        onRefresh = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insignia Empresarial") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            MetricCard {
                                Text(
                                    text = "Net worth",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoneyText(
                                    amountCents = state.summary?.netWorthCents ?: 0L,
                                    currencyCode = state.summary?.currency ?: "USD",
                                    emphasizeSign = false,
                                )
                                val pending = state.summary?.pendingSyncCount ?: 0
                                if (pending > 0) {
                                    SyncStatusChip(
                                        label = "$pending pending sync",
                                        tone = SyncTone.Pending,
                                    )
                                }
                            }
                        }
                        item {
                            Text(
                                text = "Recent activity",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        items(state.summary?.recentTransactions.orEmpty(), key = { it.id }) { tx ->
                            TransactionRow(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    MetricCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.note.ifBlank { "Transaction" }, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatMoney(tx.amountCents),
                    color = if (tx.amountCents >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            SyncStatusChip(
                label = tx.syncStatus.name.lowercase().replaceFirstChar { it.titlecase() },
                tone = when (tx.syncStatus) {
                    SyncStatus.FAILED -> SyncTone.Failed
                    SyncStatus.SYNCED -> SyncTone.Synced
                    else -> SyncTone.Pending
                },
            )
        }
    }
}
