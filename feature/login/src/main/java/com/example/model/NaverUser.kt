package com.example.model

import com.example.data.model.User

data class NaverUser(
    val id: String,
    val name: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null
)

fun NaverUser.asUser(id: String) = User(id, name, nickname, profileImageWebUrl)