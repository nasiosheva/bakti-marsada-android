package com.lampung.baktimarsada.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BaktiBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String? = null
)

@Composable
fun BaktiBottomNavigationBar(
    items: List<BaktiBottomNavItem>,
    currentRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 3.dp,
    itemColors: NavigationBarItemColors = NavigationBarItemDefaults.colors()
) {
    Surface(
        modifier = modifier.zIndex(1f),
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = tonalElevation
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = { onRouteSelected(item.route) },
                    modifier = if (item.testTag.isNullOrBlank()) {
                        Modifier
                    } else {
                        Modifier.semantics { testTag = item.testTag }
                    },
                    colors = itemColors,
                    label = { Text(text = item.label) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                )
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
