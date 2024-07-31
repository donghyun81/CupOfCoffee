package com.cupofcoffee0801.ui.commentdetail

import com.cupofcoffee0801.ui.model.CommentModel
import com.cupofcoffee0801.ui.model.UserEntry

data class CommentEditUiState(
    val userEntry: UserEntry,
    val commentModel: CommentModel?
)