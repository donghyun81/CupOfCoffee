package com.example.user

data class UserUiState(
    val userId: String = "",
    val nickName: String? = "",
    val profileUrl: String? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val attendedMeetings: List<UserMeeting> = emptyList(),
    val madeMeetings: List<UserMeeting> = emptyList(),
)

data class UserMeeting(
    val id: String,
    val date: String,
    val time: String,
    val place: String,
    val content: String
)