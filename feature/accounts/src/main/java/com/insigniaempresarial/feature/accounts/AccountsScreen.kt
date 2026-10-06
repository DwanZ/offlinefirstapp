package com.insigniaempresarial.feature.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insigniaempresarial.core.designsystem.component.MetricCard
import com.insigniaempresarial.core.designsystem.component.MoneyText
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme
import com.insigniaempresarial.core.domain.model.Account
import com.insigniaempresarial.core.domain.model.AccountType

@Composable
fun AccountsRoute(
    onAccountClick: (String) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountsScreen(state = state, onAccountClick = onAccountClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: AccountsUiState,
    onAccountClick: (String) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Accounts") }) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.accounts, key = { it.id }) { account ->
                MetricCard(
                    modifier = Modifier.clickable { onAccountClick(account.id) },
                ) {
                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = account.type.name.lowercase().replaceFirstChar { it.titlecase() },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyText(amountCents = account.balanceCents, currencyCode = account.currency, emphasizeSign = false)
                }
            }
        }
    }
}

@Preview(name = "Accounts — Dark", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun AccountsScreenPreview() {
    InsigniaTheme(darkTheme = true) {
        AccountsScreen(
            state = AccountsUiState(
                isLoading = false,
                accounts = listOf(
                    Account(
                        id = "acc_operating",
                        name = "Operating Account",
                        type = AccountType.CHECKING,
                        currency = "USD",
                        balanceCents = 1_250_000L,
                        updatedAtEpochMs = 1L,
                    ),
                    Account(
                        id = "acc_savings",
                        name = "Reserve Savings",
                        type = AccountType.SAVINGS,
                        currency = "USD",
                        balanceCents = 4_800_000L,
                        updatedAtEpochMs = 1L,
                    ),
                ),
            ),
            onAccountClick = {},
        )
    }
}
