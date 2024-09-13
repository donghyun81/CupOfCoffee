package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.data.local.model.MeetingEntity
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import kotlinx.serialization.Serializable

@Serializable
data class MeetingData(
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
    val commentIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun MeetingData.asMeetingDTO() =
    MeetingDTO(
        caption,
        lat,
        lng,
        managerId,
        personIds,
        placeId,
        date,
        time,
        createDate,
        content,
        commentIds
    )

fun MeetingData.asMeetingEntity(id: String) =
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
        content,
        commentIds
    )

fun MeetingData.asMeeting(id: String) =
    Meeting(
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
        content,
        commentIds
    )