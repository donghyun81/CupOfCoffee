package com.cupofcoffee.ui.model

data class NaverUser(
    val id: String,
    val email: String,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null
)
