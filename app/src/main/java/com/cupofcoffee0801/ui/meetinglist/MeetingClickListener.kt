package com.cupofcoffee0801.ui.meetinglist

interface MeetingClickListener {

    fun onApplyClick(meetingId: String)

    fun onCancelClick(isMyMeeting:Boolean,meetingId: String)

    fun onDetailClick(meetingId: String)
}