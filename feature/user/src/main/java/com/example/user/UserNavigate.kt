package com.example.user

interface UserNavigate {
    fun navigateMeetingDetail(meetingId: String)

    fun navigateMakeMeeting(meetingId: String)

    fun navigateSettings()

    fun navigateUserEdit()
}