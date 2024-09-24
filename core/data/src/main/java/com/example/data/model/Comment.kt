package com.example.data.model

import com.example.network.model.CommentDTO

data class Comment(
    val id: String,
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    var content: String,
    val createdDate: Long
)

fun CommentDTO.asComment(id: String) =
    Comment(id,userId, nickname, profileImageWebUrl, meetingId, content,createdDate)
