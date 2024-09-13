package com.cupofcoffee0801.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.cupofcoffee0801.R

@Composable
fun OptionsMenu(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = modifier
            .size(36.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.baseline_more_vert_24),
            contentDescription = "옵션",
        )
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "수정",
                    modifier = Modifier
                        .clickable {
                            onEditClick()
                            expanded = false
                        }
                        .padding(8.dp)
                )
                Text(
                    text = "삭제",
                    modifier = Modifier
                        .clickable {
                            onDeleteClick()
                            expanded = false
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}