package com.cupofcoffee0801.data.remote.model

import com.cupofcoffee0801.ui.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentDTO(
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    val content: String,
    val createdDate: Long
)

fun CommentDTO.asCommentEntry(id: String) =
    Comment(id,userId, nickname, profileImageWebUrl, meetingId, content,createdDate)