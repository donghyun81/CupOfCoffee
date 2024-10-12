package com.example.user

sealed class UserIntent {

    data class DeleteMeetingClick(val meetingId: String) : UserIntent()

    data class DetailMeetingClick(val meetingId: String) : UserIntent()

    data class UpdateMeetingClick(val meetingId: String) : UserIntent()

    data object SettingClick : UserIntent()

    data object UserEditClick : UserIntent()

    data object InitData : UserIntent()
}