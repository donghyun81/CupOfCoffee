package com.cupofcoffee.ui.model

import com.cupofcoffee.data.remote.MeetingDTO

data class MeetingModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val peopleId: MutableList<String>,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String
)

fun MeetingModel.toMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, peopleId, date, time, createDate, content)