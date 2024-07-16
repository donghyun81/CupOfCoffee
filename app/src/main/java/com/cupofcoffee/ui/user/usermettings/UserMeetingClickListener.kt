package com.cupofcoffee.ui.user.usermettings

import com.cupofcoffee.ui.model.MeetingEntry

interface UserMeetingClickListener {

    fun onDeleteClick(meetingEntry: MeetingEntry)

    fun onDetailClick(meetingId: String)

    fun onUpdateClick(meetingId: String)
}