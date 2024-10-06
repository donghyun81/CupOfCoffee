package com.example.commentdetail

import com.example.data.model.Comment

data class CommentUiState(
    val user: CommentEditUser = CommentEditUser(),
    val comment: Comment? = null,
    val content: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isCompleted: Boolean = false
)

data class CommentEditUser(
    val userId: String = "",
    val profileImageWebUrl: String? = null
)