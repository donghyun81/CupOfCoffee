package com.cupofcoffee0801.ui

import android.content.Context
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.cupofcoffee0801.data.DataResult
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val MEETING_TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
val MEETING_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
fun Calendar.toCurrentTime(): String = MEETING_TIME_FORMAT.format(this.time)
fun Calendar.toCurrentDate(): String = MEETING_DATE_FORMAT.format(this.time)

fun Long.isCurrentDateOver(): Boolean =
    this >= MaterialDatePicker.todayInUtcMilliseconds()


fun Long.toDateFormat(): String {
    val date = Date(this)
    val format = MEETING_DATE_FORMAT
    return format.format(date)
}

fun View.showSnackBar(stringId: Int) {
    Snackbar.make(
        this,
        stringId,
        Snackbar.LENGTH_SHORT
    ).show()
}

fun <T> View.showLoading(result: DataResult<T>) {
    visibility = when (result) {
        is DataResult.Error -> View.GONE
        is DataResult.Loading -> View.VISIBLE
        is DataResult.Success -> View.GONE
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
