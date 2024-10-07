package com.example.data.model

import com.example.database.model.MeetingEntity
import com.example.network.model.MeetingDTO
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class MeetingData(
    val placeName: String = "",
    val managerId: String = "",
    val personIds: MutableMap<String, Boolean> = mutableMapOf(),
    val placeId: String = "",
    val date: String = "",
    val time: String = "",
    val createDate: Long = 0L,
    val content: String = "",
    val commentIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun MeetingData.asMeetingDTO() =
    MeetingDTO(
        placeName,
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
        placeName,
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
        placeName,
        managerId,
        personIds,
        placeId,
        date,
        time,
        createDate,
        content,
        commentIds
    )