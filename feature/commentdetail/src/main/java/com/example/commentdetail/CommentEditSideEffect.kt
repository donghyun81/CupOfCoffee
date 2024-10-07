package com.example.commentdetail

sealed class CommentEditSideEffect {
    data object NavigateUp : CommentEditSideEffect()

    data class ShowSnackBar(val message: String) : CommentEditSideEffect()
}