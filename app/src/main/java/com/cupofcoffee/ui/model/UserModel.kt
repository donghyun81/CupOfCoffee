package com.cupofcoffee.ui.model

import com.cupofcoffee.data.remote.UserDTO

data class UserModel(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: List<String> = emptyList(),
    val attendedMeetingIds: List<String> = emptyList()
)

fun UserModel.toUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)