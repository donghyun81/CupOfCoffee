package com.cupofcoffee0801.ui.user.usermettings

import com.cupofcoffee0801.ui.model.MeetingEntry

interface UserMeetingClickListener {

    fun onDeleteClick(meetingEntry: MeetingEntry)

    fun onDetailClick(meetingId: String)

    fun onUpdateClick(meetingId: String)
}