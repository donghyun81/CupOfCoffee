package com.example.data.model

import com.example.database.model.MeetingEntity
import com.example.network.model.MeetingDTO
import kotlinx.serialization.Serializable

@Serializable
data class Meeting(
    val id: String,
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

fun Meeting.asMeetingEntity() = MeetingEntity(
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

fun Meeting.asMeetingDTO() =
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

fun MeetingDTO.asPlace(id: String) =
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

fun MeetingDTO.asMeetingEntity(id: String) = MeetingEntity(
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

fun MeetingEntity.asPlace() = Meeting(
    id,
    caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content
)

fun MeetingEntity.asMeetingDTO() =
    MeetingDTO(caption, lat, lng, managerId, personIds, placeId, date, time, createDate, content)