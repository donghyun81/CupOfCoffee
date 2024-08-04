package com.cupofcoffee0801.ui.meetingdetail

import com.cupofcoffee0801.ui.model.CommentEntry
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.UserEntry

data class MeetingDetailUiState(
    val userEntry: UserEntry,
    val meeting: MeetingEntry,
    val comments: List<CommentEntry>,
    val isMyMeeting: Boolean
)
