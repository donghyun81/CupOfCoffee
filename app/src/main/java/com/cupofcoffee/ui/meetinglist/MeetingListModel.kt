package com.cupofcoffee.ui.meetinglist

import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.UserEntry

data class MeetingListModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val people: List<UserEntry>,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String
)

fun MeetingListModel.toMeetingModel() =
    MeetingModel(
        caption,
        lat,
        lng,
        managerId,
        people.map { it.id }.toMutableList(),
        date,
        time,
        createDate,
        content
    )