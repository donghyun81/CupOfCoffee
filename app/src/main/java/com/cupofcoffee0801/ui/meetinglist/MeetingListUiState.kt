package com.cupofcoffee0801.ui.meetinglist

import com.cupofcoffee0801.ui.model.PlaceEntry

data class MeetingListUiState(
    val placeEntry: PlaceEntry,
    val meetingEntriesWithPeople: List<MeetingEntryWithPeople>
)