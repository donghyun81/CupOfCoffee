package com.example.meetingdetail

sealed class MeetingDetailIntent {

    data object HandleInitData : MeetingDetailIntent()

    data object DeleteMeeting : MeetingDetailIntent()

    data class DeleteComment(val commentId: String) : MeetingDetailIntent()

    data class EditMeeting(val meetingId: String) : MeetingDetailIntent()

    data class EditComment(val commentId: String?, val meetingId: String) : MeetingDetailIntent()
}