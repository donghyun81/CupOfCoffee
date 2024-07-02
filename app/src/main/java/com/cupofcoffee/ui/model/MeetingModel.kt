package com.cupofcoffee.ui.model

import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.ui.meetinglist.MeetingModelWithPeople

data class MeetingModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val personIds: MutableMap<String, Boolean> = mutableMapOf(),
    val placeId: String,
    val date: String,
    val time: String,
    val createDate: Long,
    val content: String,
    val isSynced: Boolean = true,
)

fun MeetingModel.asMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content)

fun MeetingModel.asMeetingEntity(id: String) =
    MeetingEntity(
        id,
        caption,
        lat,
        lng,
        managerId,
        personIds,
        placeId,
        date,
        time,
        createDate,
        content
    )

fun MeetingModel.asMeetingListModel(people: List<UserEntry>) =
    MeetingModelWithPeople(
        caption,
        lat,
        lng,
        managerId,
        people,
        placeId,
        date,
        time,
        createDate,
        content
    )