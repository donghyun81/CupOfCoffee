package com.example.meetingplace

data class MeetingPlaceUiState(
    val placeCaption: String = "",
    val snackBarMessage: String = "",
    val meetingsInPlace: List<MeetingPlaceMeetingUiModel> = emptyList(),
    val isError: Boolean = false,
    val isLoading: Boolean = false,
)

data class MeetingPlaceMeetingUiModel(
    val id: String,
    val content: String,
    val date: String,
    val time: String,
    val isAttendedMeeting: Boolean,
    val isMyMeeting: Boolean,
    val attendees: List<MeetingPlaceUserUiModel>
)

data class MeetingPlaceUserUiModel(
    val nickName: String?,
    val profilesUrl: String?
)