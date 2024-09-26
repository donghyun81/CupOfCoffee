package com.example.meetingplace

interface MeetingClickListener {

    fun onApplyClick(meetingId: String)

    fun onCancelClick(isMyMeeting:Boolean,meetingId: String)

    fun onDetailClick(meetingId: String)
}