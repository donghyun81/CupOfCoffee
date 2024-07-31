package com.cupofcoffee0801.ui.model

import android.os.Parcelable
import com.cupofcoffee0801.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MeetingsCategory(val stringResourceId: Int) : Parcelable {
    MADE_MEETINGS(R.string.made_meetings),
    ATTENDED_MEETINGS(R.string.attended_meetings);

    companion object {
        val tabCategories = entries
    }
}