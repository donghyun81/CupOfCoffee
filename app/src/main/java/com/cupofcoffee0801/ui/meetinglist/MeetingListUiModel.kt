package com.cupofcoffee0801.ui.meetinglist

data class MeetingListUiState(
    val placeCaption: String = "",
    val meetingsInPlace: List<MeetingListMeetingUiModel> = emptyList(),
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

data class MeetingListMeetingUiModel(
    val id: String,
    val content: String,
    val date: String,
    val time: String,
    val isAttendedMeeting: Boolean,
    val isMyMeeting: Boolean,
    val meetingListUserUiModel: List<MeetingListUserUiModel>
)

data class MeetingListUserUiModel(
    val nickName: String?,
    val profilesUrl: String?
)