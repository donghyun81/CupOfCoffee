package com.example.settings

sealed class SettingsSideEffect {

    data object NavigateLogin : SettingsSideEffect()

    data class ShowSnackBar(val message: String) : SettingsSideEffect()
}