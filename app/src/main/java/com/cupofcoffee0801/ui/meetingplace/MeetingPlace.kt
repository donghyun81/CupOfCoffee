package com.cupofcoffee0801.ui.meetingplace

data class MeetingPlaceUiState(
    val placeCaption: String = "",
    val meetingsInPlace: List<MeetingPlaceMeetingUiModel> = emptyList(),
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

data class MeetingPlaceMeetingUiModel(
    val id: String,
    val content: String,
    val date: String,
    val time: String,
    val isAttendedMeeting: Boolean,
    val isMyMeeting: Boolean,
    val meetingPlaceUserUiModel: List<MeetingPlaceUserUiModel>
)

data class MeetingPlaceUserUiModel(
    val nickName: String?,
    val profilesUrl: String?
)