package com.example.data.model

import com.example.network.model.CommentDTO

data class CommentData(
    val userId: String = "",
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String = "",
    var content: String = "",
    val createdDate: Long = 0
)

fun CommentData.asCommentDTO() =
    CommentDTO(userId, nickname, profileImageWebUrl, meetingId, content, createdDate)