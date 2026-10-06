package com.insigniaempresarial.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.insigniaempresarial.core.designsystem.theme.InsigniaThemeExtras
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

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

fun formatMoney(amountCents: Long, currencyCode: String = "USD"): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance(currencyCode)
    }
    return format.format(amountCents / 100.0)
}
