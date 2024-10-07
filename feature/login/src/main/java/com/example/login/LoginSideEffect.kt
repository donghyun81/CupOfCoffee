package com.example.login

sealed class LoginSideEffect {
    data object NavigateHome : LoginSideEffect()

    data class ShowSnackBar(val message: String) : LoginSideEffect()
}