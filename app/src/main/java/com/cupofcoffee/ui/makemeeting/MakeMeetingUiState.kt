package com.cupofcoffee.ui.makemeeting

import com.cupofcoffee.ui.model.MeetingEntry

data class MakeMeetingUiState(
    val placeName: String,
    val lat: Double,
    val lng: Double,
    val meetingEntry: MeetingEntry?
)