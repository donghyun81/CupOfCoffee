package com.cupofcoffee.data.remote.model

import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun UserDTO.asUserEntry(id: String): UserEntry {
    return UserEntry(
        id,
        UserModel(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)
    )
}

fun UserDTO.asUserModel() =
    UserModel(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

