package com.example.login

sealed class LoginIntent {
    data object LoginButtonClicked : LoginIntent()
}