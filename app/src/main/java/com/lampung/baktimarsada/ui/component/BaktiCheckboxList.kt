package com.lampung.baktimarsada.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BaktiCheckboxList(
    selectedItems: Set<String>,
    options: List<String>,
    onItemToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        options.forEach { option ->
            BaktiCheckbox(
                checked = selectedItems.contains(option),
                label = option,
                onCheckedChange = { onItemToggle(option) }
            )
        }
    }
}
