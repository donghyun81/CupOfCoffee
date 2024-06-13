package com.cupofcoffee.ui

import android.view.View
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MEETING_DATE_FORMAT = "yyyy-MM-dd"
private const val MEETING_TIME_FORMAT = "HH:mm"

fun Calendar.toCurrentTime(): String =
    SimpleDateFormat(MEETING_TIME_FORMAT).format(this.time)

fun Calendar.toCurrentDate(): String = SimpleDateFormat(MEETING_DATE_FORMAT).format(this.time)

fun Long.isCurrentDateOver(): Boolean =
    this >= MaterialDatePicker.todayInUtcMilliseconds()


fun Long.toDateFormat(): String {
    val date = Date(this)
    val format = SimpleDateFormat(MEETING_DATE_FORMAT, Locale.getDefault())
    return format.format(date)
}

fun View.showSnackBar(stringId: Int) {
    Snackbar.make(
        this,
        stringId,
        Snackbar.LENGTH_SHORT
    ).show()
}