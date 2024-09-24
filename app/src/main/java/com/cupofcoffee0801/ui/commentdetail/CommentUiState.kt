package com.cupofcoffee0801.ui.commentdetail

import com.example.data.model.Comment

data class CommentUiState(
    val user: CommentEditUser? = null,
    val comment: Comment? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isCompleted:Boolean = false
)

data class CommentEditUser(
    val userId: String,
    val profileImageWebUrl: String?
)