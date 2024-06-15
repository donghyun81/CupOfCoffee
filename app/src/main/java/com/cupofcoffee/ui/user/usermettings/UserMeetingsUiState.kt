package com.cupofcoffee.ui.user.usermettings

import com.cupofcoffee.ui.model.MeetingEntry

data class UserMeetingsUiState(
    val meetings: List<MeetingEntry> = emptyList()
)