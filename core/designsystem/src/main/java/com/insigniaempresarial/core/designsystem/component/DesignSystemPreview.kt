package com.insigniaempresarial.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme

@Preview(name = "Design system — Dark", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun DesignSystemPreview() {
    InsigniaTheme(darkTheme = true) {
        MetricCard(modifier = Modifier.padding(16.dp)) {
            Text("Net worth", color = MaterialTheme.colorScheme.onSurfaceVariant)
            MoneyText(amountCents = 6_085_000L, emphasizeSign = false)
            SyncStatusChip(label = "2 pending sync", tone = SyncTone.Pending)
            InsigniaPrimaryButton(text = "Sync now", onClick = {})
        }
    }
}
