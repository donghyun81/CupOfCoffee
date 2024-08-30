package com.cupofcoffee0801.ui.meetingplace

interface MeetingClickListener {

    fun onApplyClick(meetingId: String)

    fun onCancelClick(isMyMeeting:Boolean,meetingId: String)

    fun onDetailClick(meetingId: String)
}