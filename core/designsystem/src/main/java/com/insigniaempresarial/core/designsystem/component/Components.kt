package com.insigniaempresarial.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.insigniaempresarial.core.designsystem.theme.InsigniaThemeExtras
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class SyncTone { Pending, Failed, Synced }

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun MoneyText(
    amountCents: Long,
    currencyCode: String = "USD",
    modifier: Modifier = Modifier,
    emphasizeSign: Boolean = true,
) {
    val extras = InsigniaThemeExtras.colors
    val color = when {
        !emphasizeSign -> MaterialTheme.colorScheme.onSurface
        amountCents > 0 -> extras.profit
        amountCents < 0 -> extras.loss
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = formatMoney(amountCents, currencyCode),
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Composable
fun SyncStatusChip(
    label: String,
    tone: SyncTone,
    modifier: Modifier = Modifier,
) {
    val extras = InsigniaThemeExtras.colors
    val (container, content) = when (tone) {
        SyncTone.Pending -> extras.pending.copy(alpha = 0.2f) to extras.pending
        SyncTone.Failed -> extras.loss.copy(alpha = 0.2f) to extras.loss
        SyncTone.Synced -> extras.profit.copy(alpha = 0.15f) to extras.profit
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content,
        ),
    )
}

@Composable
fun InsigniaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text)
    }
}

fun formatMoney(amountCents: Long, currencyCode: String = "USD"): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance(currencyCode)
    }
    return format.format(amountCents / 100.0)
}
