package com.cupofcoffee0801.ui.login


data class LoginUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isComplete: Boolean = false
)