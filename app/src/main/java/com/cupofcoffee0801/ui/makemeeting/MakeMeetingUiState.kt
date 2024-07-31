package com.cupofcoffee0801.ui.makemeeting

import com.cupofcoffee0801.ui.model.MeetingEntry

data class MakeMeetingUiState(
    val placeName: String,
    val lat: Double,
    val lng: Double,
    val meetingEntry: MeetingEntry?
)