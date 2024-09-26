package com.example.model

import android.os.Parcelable
import com.example.user.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MeetingsCategory(val stringResourceId: Int) : Parcelable {
    MADE_MEETINGS(R.string.made_meetings),
    ATTENDED_MEETINGS(R.string.attended_meetings);

    companion object {
        val tabCategories = entries
    }
}