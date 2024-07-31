package com.cupofcoffee0801.ui.user.usermettings

import com.cupofcoffee0801.ui.model.MeetingEntry

data class UserMeetingsUiState(
    val meetings: List<MeetingEntry> = emptyList()
)