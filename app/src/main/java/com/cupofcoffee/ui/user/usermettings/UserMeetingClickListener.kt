package com.cupofcoffee.ui.user.usermettings

import com.cupofcoffee.ui.model.MeetingEntry

interface UserMeetingClickListener {

    fun onClick(meetingEntry: MeetingEntry)
}