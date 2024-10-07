package com.example.meetingdetail

interface MeetingDetailNavigate {

    fun navigateMakeMeeting(meetindId: String)

    fun navigateUp()

    fun navigateCommentEdit(commentId: String?, meetingId: String)
}