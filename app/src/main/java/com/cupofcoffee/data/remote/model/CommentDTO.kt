package com.cupofcoffee.data.remote.model

import com.cupofcoffee.ui.model.CommentEntry
import com.cupofcoffee.ui.model.CommentModel
import kotlinx.serialization.Serializable

@Serializable
data class CommentDTO(
    val userId: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val meetingId: String,
    val content: String
)

fun CommentDTO.asCommentEntry(id: String) =
    CommentEntry(id, CommentModel(userId, nickname, profileImageWebUrl, meetingId, content))