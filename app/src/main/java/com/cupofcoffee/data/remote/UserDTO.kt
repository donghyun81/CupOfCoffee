package com.cupofcoffee.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val createdMeetingIds: List<String> = emptyList(),
    val attendedMeetingIds: List<String> = emptyList()
)