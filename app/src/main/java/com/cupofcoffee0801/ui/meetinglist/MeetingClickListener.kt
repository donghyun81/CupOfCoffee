package com.cupofcoffee0801.ui.meetinglist

interface MeetingClickListener {

    fun onApplyClick(meetingId: String)

    fun onCancelClick(meetingId: String)

    fun onDetailClick(meetingId: String)
}