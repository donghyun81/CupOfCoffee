package com.cupofcoffee0801.ui.model

data class Comment(
    val id: String,
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    var content: String,
    val createdDate: Long
)

fun Comment.asCommentData() =
    CommentData(userId, nickname, profileImageWebUrl, meetingId, content, createdDate)