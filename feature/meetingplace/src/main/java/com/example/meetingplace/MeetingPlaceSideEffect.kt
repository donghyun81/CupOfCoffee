package com.example.meetingplace

sealed class MeetingPlaceSideEffect {
    data class NavigateMeetingDetail(val meetingId: String) : MeetingPlaceSideEffect()

    data object NavigateUp : MeetingPlaceSideEffect()

    data class ShowSnackBar(val message: String) : MeetingPlaceSideEffect()
}