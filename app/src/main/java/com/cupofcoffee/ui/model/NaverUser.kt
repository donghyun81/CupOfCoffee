package com.cupofcoffee.ui.model

data class NaverUser(
    val id: String,
    val name: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null
)

fun NaverUser.toUserModel() = UserModel(name, nickname, profileImageWebUrl)

fun NaverUser.toUserEntry(id: String) = UserEntry(id, toUserModel())