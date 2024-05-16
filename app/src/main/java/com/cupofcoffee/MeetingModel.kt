package com.cupofcoffee

data class MeetingModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val peopleId: List<String>,
    val time: Long,
    val createDate: Long,
    val content: String
)

fun MeetingModel.toMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, peopleId, time, createDate, content)