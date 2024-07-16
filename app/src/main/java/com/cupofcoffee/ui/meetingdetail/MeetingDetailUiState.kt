package com.cupofcoffee.ui.meetingdetail

import com.cupofcoffee.ui.model.CommentEntry
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.UserEntry

data class MeetingDetailUiState(
    val userEntry: UserEntry,
    val meeting: MeetingEntry,
    val comments: List<CommentEntry>,
    val isMyMeeting: Boolean
)
