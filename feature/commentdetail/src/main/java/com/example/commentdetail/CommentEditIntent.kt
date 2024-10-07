package com.example.commentdetail

sealed class CommentEditIntent {

    data object EditComment : CommentEditIntent()

    data object InitData : CommentEditIntent()

    data class EnterContent(val content: String) : CommentEditIntent()
}