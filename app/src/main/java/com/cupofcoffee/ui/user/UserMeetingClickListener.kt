package com.cupofcoffee.ui.user

import com.cupofcoffee.ui.model.MeetingEntry

interface UserMeetingClickListener {

    fun onClick(meetingEntry: MeetingEntry)
}