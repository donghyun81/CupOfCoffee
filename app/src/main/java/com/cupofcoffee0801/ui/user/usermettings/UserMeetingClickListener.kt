package com.cupofcoffee0801.ui.user.usermettings

import com.cupofcoffee0801.ui.model.Meeting

interface UserMeetingClickListener {

    fun onDeleteClick(meeting: Meeting)

    fun onDetailClick(meetingId: String)

    fun onUpdateClick(meetingId: String)
}