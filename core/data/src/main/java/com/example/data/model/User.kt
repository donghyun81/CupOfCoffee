package com.example.data.model

import com.example.database.model.UserEntity
import com.example.network.model.UserDTO

data class User(
    val id: String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun User.asUserEntity() =
    UserEntity(id, name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)


fun User.asUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

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

fun UserEntity.asUserEntry() =
    User(id,name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

fun UserEntity.asUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds)