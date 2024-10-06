package com.example.makemeeting

sealed class MakeMeetingIntent {

    data object InitData : MakeMeetingIntent()
    data class EnterContent(val content: String) : MakeMeetingIntent()

    data class EditDate(val date: String) : MakeMeetingIntent()

    data class EditTime(val time: String) : MakeMeetingIntent()

    data object MakeMeeting : MakeMeetingIntent()
}