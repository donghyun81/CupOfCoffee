package com.cupofcoffee.ui.meetinglist

import com.cupofcoffee.ui.model.PlaceEntry

data class MeetingListUiState(
    val placeEntry: PlaceEntry? = null,
    val meetingEntriesWithPeople: List<MeetingEntryWithPeople> = emptyList()
)