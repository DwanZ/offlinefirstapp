package com.insigniaempresarial.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.designsystem.component.InsigniaPrimaryButton
import com.insigniaempresarial.core.designsystem.component.MetricCard
import com.insigniaempresarial.core.designsystem.component.SyncStatusChip
import com.insigniaempresarial.core.designsystem.component.SyncTone
import com.insigniaempresarial.core.designsystem.component.formatMoney
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.AccountType
import com.insigniaempresarial.core.domain.model.Transaction

@Composable
fun TransactionsRoute(
    onMessage: (String) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TransactionsEffect.Message -> onMessage(effect.text)
            }
        }
    }
    TransactionsScreen(
        state = state,
        onFilter = viewModel::onFilterSelected,
        onShowAdd = viewModel::onShowAdd,
        onAdd = viewModel::onAdd,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onFilter: (TransactionFilter) -> Unit,
    onShowAdd: (Boolean) -> Unit,
    onAdd: (accountId: String, amountCents: Long, note: String) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Transactions") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onShowAdd(true) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TransactionFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { onFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.titlecase() }) },
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.transactions, key = { it.id }) { tx ->
                    MetricCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.note.ifBlank { "Transaction" }, style = MaterialTheme.typography.titleMedium)
                                Text(formatMoney(tx.amountCents))
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
            }
        }
    }

    if (state.showAddSheet) {
        AddTransactionSheet(
            accounts = state.accounts.map { it.id to it.name },
            onDismiss = { onShowAdd(false) },
            onConfirm = onAdd,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    accounts: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: String, amountCents: Long, note: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expanded by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.first.orEmpty()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add transaction", style = MaterialTheme.typography.titleLarge)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = accounts.firstOrNull { it.first == selectedAccount }?.second.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    accounts.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedAccount = id
                                expanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (e.g. -45.00 or 120.50)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
            )
            InsigniaPrimaryButton(
                text = "Save offline",
                onClick = {
                    val dollars = amountText.toDoubleOrNull() ?: return@InsigniaPrimaryButton
                    val cents = (dollars * 100).toLong()
                    onConfirm(selectedAccount, cents, note)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAccount.isNotBlank() && amountText.toDoubleOrNull() != null,
            )
        }
    }
}

@Preview(name = "Transactions — Dark", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun TransactionsScreenPreview() {
    InsigniaTheme(darkTheme = true) {
        TransactionsScreen(
            state = TransactionsUiState(
                isLoading = false,
                filter = TransactionFilter.ALL,
                transactions = listOf(
                    Transaction(
                        id = "tx_1",
                        accountId = "acc_operating",
                        amountCents = -45_000L,
                        categoryId = "cat_ops",
                        note = "Office supplies",
                        bookedAtEpochMs = 1L,
                        syncStatus = SyncStatus.SYNCED,
                        clientMutationId = "m1",
                        updatedAtEpochMs = 1L,
                    ),
                    Transaction(
                        id = "tx_2",
                        accountId = "acc_operating",
                        amountCents = 320_000L,
                        categoryId = "cat_revenue",
                        note = "Client invoice #1042",
                        bookedAtEpochMs = 2L,
                        syncStatus = SyncStatus.PENDING,
                        clientMutationId = "m2",
                        updatedAtEpochMs = 2L,
                    ),
                ),
                accounts = listOf(
                    Account(
                        id = "acc_operating",
                        name = "Operating Account",
                        type = AccountType.CHECKING,
                        currency = "USD",
                        balanceCents = 1_250_000L,
                        updatedAtEpochMs = 1L,
                    ),
                ),
            ),
            onFilter = {},
            onShowAdd = {},
            onAdd = { _, _, _ -> },
        )
    }
}
