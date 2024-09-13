package com.cupofcoffee0801.data.remote.model

import com.cupofcoffee0801.data.local.model.UserEntity
import com.cupofcoffee0801.ui.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun UserDTO.asUserEntry(id: String): User {
    return User(
        id,
        name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds
    )
}

fun UserDTO.asUserEntity(id: String): UserEntity {
    return UserEntity(
        id, name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds
    )
}