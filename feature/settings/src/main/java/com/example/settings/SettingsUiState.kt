package com.example.settings

data class SettingsUiState(
    val isAutoLogin: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
)