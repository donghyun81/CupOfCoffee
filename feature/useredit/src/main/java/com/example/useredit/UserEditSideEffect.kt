package com.example.useredit

sealed class UserEditSideEffect {

    data object NavigateUp : UserEditSideEffect()

    data class ShowSnackBar(val message:String) : UserEditSideEffect()
}