package com.cupofcoffee0801.ui.user

import com.cupofcoffee0801.ui.model.UserEntry

data class UserUiState(
    val user: UserEntry? = null,
    val attendedMeetingsCount: Int = 0,
    val madeMeetingsCount: Int = 0
)