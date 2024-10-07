package com.example.settings

sealed class SettingsIntent {

    data object SwitchAutoLogin : SettingsIntent()

    data object Logout : SettingsIntent()

    data object CancelMembership : SettingsIntent()

    data object InitData : SettingsIntent()
}