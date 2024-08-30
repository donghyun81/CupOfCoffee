package com.cupofcoffee0801.ui.user.usermettings

import com.cupofcoffee0801.ui.model.Meeting

data class UserMeetingsUiState(
    val meetings: List<Meeting> = emptyList()
)