package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val createdMeetingIds: List<String> = emptyList(),
    val attendedMeetingIds: List<String> = emptyList()
)

fun UserDTO.toUserEntry(id: String): UserEntry {
    return UserEntry(
        id,
        UserModel(name, nickname, profileImageWebUrl, createdMeetingIds, attendedMeetingIds)
    )
}

fun UserDTO.toUserModel() =
    UserModel(name, nickname, profileImageWebUrl, createdMeetingIds, attendedMeetingIds)

