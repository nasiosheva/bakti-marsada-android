package com.lampung.baktimarsada.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaktiBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp),
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        ) {}
                    }
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sheet"
                    )
                }
            }

            title?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom

@Preview(name = "BaktiBottomSheet - With Title", showBackground = true)
@Composable
private fun BaktiBottomSheetWithTitlePreview() {
    BaktiMarsadaTheme {
        BaktiBottomSheet(
            onDismissRequest = {},
            title = "Tambah Data"
        ) {
            Text(text = "Konten bottom sheet")
        }
    }
}

@Preview(name = "BaktiBottomSheet - No Title", showBackground = true)
@Composable
private fun BaktiBottomSheetNoTitlePreview() {
    BaktiMarsadaTheme {
        BaktiBottomSheet(
            onDismissRequest = {}
        ) {
            Text(text = "Konten bottom sheet tanpa title")
        }
    }
}
