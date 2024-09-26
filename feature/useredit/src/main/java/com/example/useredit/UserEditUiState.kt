package com.example.useredit

data class UserEditUiState(
    val userId: String = "",
    val nickname: String? = null,
    val contentUri: String? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isCompleted:Boolean = false
)