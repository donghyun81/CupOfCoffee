package com.example.meetingdetail

sealed class MeetingDetailSideEffect {

    data class NavigateToMakeMeeting(val meetingId: String) : MeetingDetailSideEffect()

    data class NavigateToCommentEdit(val commentId: String?, val meetingId: String) :
        MeetingDetailSideEffect()

    data class ShowSnackBar(val message: String) : MeetingDetailSideEffect()
}