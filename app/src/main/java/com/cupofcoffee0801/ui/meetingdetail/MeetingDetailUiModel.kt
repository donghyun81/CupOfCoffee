package com.cupofcoffee0801.ui.meetingdetail

data class MeetingDetailUiState(
    val userUiModel: MeetingDetailUserUiModel = MeetingDetailUserUiModel(),
    val meetingUiModel: MeetingDetailMeetingUiModel = MeetingDetailMeetingUiModel(),
    val comments: List<MeetingDetailCommentUiModel> = emptyList(),
    val isMyMeeting: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

data class MeetingDetailUserUiModel(
    val id: String = "",
    val profileUrl: String? = null
)

data class MeetingDetailMeetingUiModel(
    val id: String = "",
    val content: String = "",
    val caption: String = "",
    val date: String = "",
    val time: String = ""
)

data class MeetingDetailCommentUiModel(
    val id: String = "",
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    var content: String = "",
    val createdDate: Long = 0L
)