package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableList<String> = mutableListOf(),
    val attendedMeetingIds: MutableList<String> = mutableListOf()
)

fun UserDTO.toUserEntry(id: String): UserEntry {
    return UserEntry(
        id,
        UserModel(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)
    )
}

fun UserDTO.toUserModel() =
    UserModel(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

