package com.example.makemeeting

sealed class MakeMeetingSideEffect {
    data object NavigateUp : MakeMeetingSideEffect()
    data class ShowSnackBar(val message: String) : MakeMeetingSideEffect()
}