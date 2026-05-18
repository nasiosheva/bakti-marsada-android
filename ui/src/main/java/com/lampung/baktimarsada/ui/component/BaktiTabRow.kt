package com.lampung.baktimarsada.ui.component

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> BaktiTabRow(
    selectedTab: T,
    tabs: List<Pair<T, String>>,
    onSelectedTabChange: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }

    TabRow(
        modifier = modifier,
        selectedTabIndex = if (selectedIndex >= 0) selectedIndex else 0
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onSelectedTabChange(tab) },
                text = { Text(text = label) }
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
