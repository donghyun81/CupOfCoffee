package com.cupofcoffee.ui.model

data class NaverUser(
    val id: String,
    val name: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null
)

fun NaverUser.tasUserModel() = UserModel(name, nickname, profileImageWebUrl)

fun NaverUser.asUserEntry(id: String) = UserEntry(id, tasUserModel())