package com.cupofcoffee0801.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> StateContent(
    modifier: Modifier = Modifier
        .fillMaxSize(),
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

            isComplete -> {
                SideEffect {
                    navigateUp()
                }
            }

            else -> {
                content(data)
            }
        }
    }
}