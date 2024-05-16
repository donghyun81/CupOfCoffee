package com.cupofcoffee.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class MeetingDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val managerId: String,
    val peopleId: List<String>,
    val time:Long,
    val createDate:Long,
    val content:String
)