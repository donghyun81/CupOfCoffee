package com.cupofcoffee0801.ui.user

import com.cupofcoffee0801.ui.model.User

data class UserUiState(
    val user: User? = null,
    val attendedMeetingsCount: Int = 0,
    val madeMeetingsCount: Int = 0
)