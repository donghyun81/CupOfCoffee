package com.example.user

sealed class UserSideEffect {
    data class NavigateMeetingDetail(val meetingId: String) : UserSideEffect()

    data class NavigateMakeMeeting(val meetingId: String) : UserSideEffect()

    data object NavigateSettings : UserSideEffect()

    data object NavigateUserEdit : UserSideEffect()

}