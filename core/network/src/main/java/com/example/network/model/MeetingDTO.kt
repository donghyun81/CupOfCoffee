package com.example.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MeetingDTO(
    val placeName: String,
    val managerId: String,
    val personIds: MutableMap<String, Boolean> = mutableMapOf(),
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String,
    val commentIds: MutableMap<String, Boolean> = mutableMapOf()
)
