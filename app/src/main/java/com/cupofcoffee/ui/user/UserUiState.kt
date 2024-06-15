package com.cupofcoffee.ui.user

import com.cupofcoffee.ui.model.UserEntry

data class UserUiState(
    val user: UserEntry? = null,
    val attendedMeetingsCount: Int = 0,
    val madeMeetingsCount: Int = 0
)