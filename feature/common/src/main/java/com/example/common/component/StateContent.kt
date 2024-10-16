package com.example.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <T> StateContent(
    modifier: Modifier = Modifier,
    isError: Boolean,
    isLoading: Boolean,
    isComplete: Boolean = false,
    navigateUp: () -> Unit = {},
    data: T,
    content: @Composable (T) -> Unit,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            isError -> {
                Text(
                    text = "데이터 요청중에 오류가 발생했습니다!",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                content(data)
            }
        }
    }
}