package com.cupofcoffee0801.ui.commentdetail

import com.cupofcoffee0801.ui.model.Comment
import com.cupofcoffee0801.ui.model.User

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