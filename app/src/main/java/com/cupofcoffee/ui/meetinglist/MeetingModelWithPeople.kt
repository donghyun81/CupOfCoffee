package com.cupofcoffee.ui.meetinglist

import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.UserEntry

data class MeetingModelWithPeople(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val people: List<UserEntry>,
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String,
    val isSynced: Boolean = false
)

fun MeetingModelWithPeople.asMeetingModel() =
    MeetingModel(
        caption,
        lat,
        lng,
        managerId,
        people.associate { it.id to true }.toMutableMap(),
        placeId,
        date,
        time,
        createDate,
        content
    )