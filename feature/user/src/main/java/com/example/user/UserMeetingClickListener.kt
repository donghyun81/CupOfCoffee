package com.example.user

interface UserMeetingClickListener {

    fun onDeleteClick(meetingId: String)

    fun onDetailClick(meetingId: String)

    fun onUpdateClick(meetingId: String)
}