package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.data.local.model.UserEntity
import com.cupofcoffee0801.data.remote.model.UserDTO

data class UserModel(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun UserModel.asUserEntity(id: String) =
    UserEntity(id, name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

fun UserModel.asUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)