package com.cupofcoffee.ui.model

import com.cupofcoffee.data.remote.model.CommentDTO

data class CommentModel(
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    var content: String,
    val createdDate: Long
)

fun CommentModel.asCommentDTO() =
    CommentDTO(userId, nickname, profileImageWebUrl, meetingId, content, createdDate)