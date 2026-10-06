package com.insigniaempresarial.feature.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insigniaempresarial.core.designsystem.component.InsigniaPrimaryButton
import com.insigniaempresarial.core.designsystem.component.MetricCard
import com.insigniaempresarial.core.designsystem.component.SyncStatusChip
import com.insigniaempresarial.core.designsystem.component.SyncTone
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme
import com.insigniaempresarial.core.domain.model.SyncHealth

@Composable
fun SyncRoute(
    onMessage: (String) -> Unit,
    viewModel: SyncViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SyncEffect.Message -> onMessage(effect.text)
            }
        }
    }
    SyncScreen(
        state = state,
        onSyncNow = viewModel::onSyncNow,
        onRetryFailed = viewModel::onRetryFailed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    state: SyncUiState,
    onSyncNow: () -> Unit,
    onRetryFailed: () -> Unit,
) {
    val health = state.health
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Sync health") }) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard {
                Text("Last successful sync", style = MaterialTheme.typography.titleMedium)
                Text(state.lastSyncLabel)
                Text("Pending: ${health?.pendingCount ?: 0}")
                Text("Failed: ${health?.failedCount ?: 0}")
                if (health?.isSyncing == true) {
                    SyncStatusChip(label = "Syncing", tone = SyncTone.Pending)
                }
                health?.lastError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
            InsigniaPrimaryButton(
                text = "Sync now",
                onClick = onSyncNow,
                modifier = Modifier.fillMaxWidth(),
            )
            InsigniaPrimaryButton(
                text = "Retry failed",
                onClick = onRetryFailed,
                modifier = Modifier.fillMaxWidth(),
                enabled = (health?.failedCount ?: 0) > 0,
            )
            Text(
                text = "Conflict policy: last-write-wins by updatedAt (v1).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Sync — Dark", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun SyncScreenPreview() {
    InsigniaTheme(darkTheme = true) {
        SyncScreen(
            state = SyncUiState(
                health = SyncHealth(
                    pendingCount = 2,
                    failedCount = 1,
                    lastSuccessfulSyncAtEpochMs = 1_727_900_000_000L,
                    lastError = "mock upstream failure",
                    isSyncing = false,
                ),
                lastSyncLabel = "Oct 3, 2026, 12:00 PM",
            ),
            onSyncNow = {},
            onRetryFailed = {},
        )
    }
}
