package com.cupofcoffee0801.ui.commentdetail

import com.cupofcoffee0801.ui.model.CommentData
import com.cupofcoffee0801.ui.model.User

data class CommentEditUiState(
    val user: User,
    val commentData: CommentData?
)