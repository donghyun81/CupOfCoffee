package com.cupofcoffee.ui.model

import com.cupofcoffee.data.remote.MeetingDTO
import com.cupofcoffee.ui.meetinglist.MeetingListModel

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

fun MeetingModel.toMeetingListModel(people: List<UserEntry>) =
    MeetingListModel(caption, lat, lng, managerId, people, date, time, createDate, content)