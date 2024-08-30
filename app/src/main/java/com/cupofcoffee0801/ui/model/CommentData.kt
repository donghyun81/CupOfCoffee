package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.data.remote.model.CommentDTO

data class CommentData(
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    var content: String,
    val createdDate: Long
)

fun CommentData.asCommentDTO() =
    CommentDTO(userId, nickname, profileImageWebUrl, meetingId, content, createdDate)