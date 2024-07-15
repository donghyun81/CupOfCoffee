package com.cupofcoffee.ui.commentdetail

import com.cupofcoffee.ui.model.CommentModel
import com.cupofcoffee.ui.model.UserEntry

data class CommentEditUiState(
    val userEntry: UserEntry,
    val commentModel: CommentModel?
)