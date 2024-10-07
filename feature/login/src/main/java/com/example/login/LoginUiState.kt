package com.example.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isLoginButtonEnable: Boolean = true,
    val isShowSnackBar: Boolean = false
)