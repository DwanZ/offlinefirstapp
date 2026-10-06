package com.insigniaempresarial.core.designsystem.component

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.insigniaempresarial.core.designsystem.theme.InsigniaThemeExtras

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
