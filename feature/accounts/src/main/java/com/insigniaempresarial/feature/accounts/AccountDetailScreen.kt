package com.insigniaempresarial.feature.accounts

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insigniaempresarial.core.common.SyncStatus
import com.insigniaempresarial.core.designsystem.component.InsigniaPrimaryButton
import com.insigniaempresarial.core.designsystem.component.MetricCard
import com.insigniaempresarial.core.designsystem.component.MoneyText
import com.insigniaempresarial.core.designsystem.component.formatMoney
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.AccountType
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import com.insigniaempresarial.core.domain.model.Transaction

@Composable
fun AccountDetailRoute(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AccountDetailEffect.Message -> onMessage(effect.text)
            }
        }
    }
    AccountDetailScreen(
        state = state,
        onBack = onBack,
        onShowTransfer = viewModel::onShowTransfer,
        onTransfer = viewModel::onTransfer,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    state: AccountDetailUiState,
    onBack: () -> Unit,
    onShowTransfer: (Boolean) -> Unit,
    onTransfer: (toAccountId: String, amountCents: Long, note: String) -> Unit,
) {
    val account = state.account
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(account?.name ?: "Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MetricCard {
                    Text("Balance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    MoneyText(
                        amountCents = account?.balanceCents ?: 0L,
                        currencyCode = account?.currency ?: "USD",
                        emphasizeSign = false,
                    )
                    InsigniaPrimaryButton(
                        text = "Transfer",
                        onClick = { onShowTransfer(true) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (state.budgets.isNotEmpty()) {
                item {
                    Text("Budgets", style = MaterialTheme.typography.titleLarge)
                }
                items(state.budgets, key = { it.id }) { budget ->
                    val categoryName = state.categories.firstOrNull { it.id == budget.categoryId }?.name
                        ?: budget.categoryId
                    MetricCard {
                        Text(categoryName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Limit ${formatMoney(budget.limitCents)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                Text("Ledger", style = MaterialTheme.typography.titleLarge)
            }
            items(state.ledger, key = { it.id }) { tx ->
                MetricCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(tx.note.ifBlank { "Transaction" })
                        Text(formatMoney(tx.amountCents))
                    }
                }
            }
        }
    }

    if (state.showTransfer && account != null) {
        TransferSheet(
            fromAccountName = account.name,
            destinations = state.allAccounts.filter { it.id != account.id },
            onDismiss = { onShowTransfer(false) },
            onConfirm = onTransfer,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferSheet(
    fromAccountName: String,
    destinations: List<com.insigniaempresarial.core.domain.model.Account>,
    onDismiss: () -> Unit,
    onConfirm: (toAccountId: String, amountCents: Long, note: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expanded by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf(destinations.firstOrNull()?.id.orEmpty()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Transfer from $fromAccountName", style = MaterialTheme.typography.titleLarge)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = destinations.firstOrNull { it.id == selectedId }?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    destinations.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                selectedId = account.id
                                expanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (e.g. 50.00)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
            )
            InsigniaPrimaryButton(
                text = "Transfer offline",
                onClick = {
                    val dollars = amountText.toDoubleOrNull() ?: return@InsigniaPrimaryButton
                    onConfirm(selectedId, (dollars * 100).toLong(), note)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedId.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
            )
        }
    }
}

@Preview(name = "Account detail — Dark", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun AccountDetailScreenPreview() {
    InsigniaTheme(darkTheme = true) {
        AccountDetailScreen(
            state = AccountDetailUiState(
                isLoading = false,
                account = Account(
                    id = "acc_operating",
                    name = "Operating Account",
                    type = AccountType.CHECKING,
                    currency = "USD",
                    balanceCents = 1_250_000L,
                    updatedAtEpochMs = 1L,
                ),
                ledger = listOf(
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
                ),
                budgets = listOf(
                    Budget(
                        id = "bud_ops",
                        categoryId = "cat_ops",
                        limitCents = 200_000L,
                        periodStartEpochMs = 1L,
                        periodEndEpochMs = 2L,
                    ),
                ),
                categories = listOf(Category(id = "cat_ops", name = "Operations", iconKey = "build")),
            ),
            onBack = {},
            onShowTransfer = {},
            onTransfer = { _, _, _ -> },
        )
    }
}
