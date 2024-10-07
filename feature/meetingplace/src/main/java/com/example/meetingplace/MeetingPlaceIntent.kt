package com.example.meetingplace

sealed class MeetingPlaceIntent {
    data class MeetingApplyClick(val meetingId: String) : MeetingPlaceIntent()

    data class AttendedCancelClick(val isMyMeeting: Boolean, val meetingId: String) :
        MeetingPlaceIntent()

    data class MeetingDetailClick(val meetingId: String) : MeetingPlaceIntent()
}