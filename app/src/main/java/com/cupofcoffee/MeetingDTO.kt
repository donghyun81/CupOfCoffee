package com.cupofcoffee

import kotlinx.serialization.Serializable

@Serializable
data class MeetingDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val personnel: Int,
    val managerId: String,
    val peopleId: List<String>,
    val time:Long,
    val createDate:Long,
    val content:String
)