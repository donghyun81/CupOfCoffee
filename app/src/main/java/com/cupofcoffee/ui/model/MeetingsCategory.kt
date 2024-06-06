package com.cupofcoffee.ui.model

import android.os.Parcelable
import com.cupofcoffee.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MeetingsCategory(val stringResourceId: Int):Parcelable {
    ATTENDED_MEETINGS(R.string.attended_meetings),
    MADE_MEETINGS(R.string.made_meetings);

    companion object {
        val tabCategories = entries
    }
}