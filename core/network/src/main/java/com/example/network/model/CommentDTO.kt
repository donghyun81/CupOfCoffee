package com.example.network.model

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