package com.cupofcoffee0801.ui.makemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.graphics.Black
import com.cupofcoffee0801.ui.graphics.Brown
import com.cupofcoffee0801.ui.isCurrentDateOver
import com.cupofcoffee0801.ui.showSnackBar
import com.cupofcoffee0801.ui.toCurrentTime
import com.cupofcoffee0801.ui.toDateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class MakeMeetingFragment : BottomSheetDialogFragment() {

    private val viewModel: MakeMeetingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MakeMeetingScreen(
                    viewModel,
                    ::showMeetingDatePicker,
                    ::showMeetingTimePicker,
                    ::navigateUp,
                )
            }
        }
    }

    private fun showMeetingDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(
            getString(R.string.date_picker_title)
        )
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
        datePicker.show(parentFragmentManager, getString(R.string.meeting_date_picker_tag))
        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            if (selectedDateInMillis.isCurrentDateOver())
                viewModel.updateDate(selectedDateInMillis.toDateFormat())
            else view?.showSnackBar(R.string.select_previous_date)
        }
    }

    private fun showMeetingTimePicker() {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.updateTime(calendar.toCurrentTime())
        }
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@Composable
fun MakeMeetingScreen(
    viewModel: MakeMeetingViewModel = hiltViewModel(),
    showDatePicker: () -> Unit,
    showTimePicker: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState()
    val isButtonClicked by viewModel.isButtonClicked.observeAsState(false)
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState!!.content,
                    onValueChange = { newContent -> viewModel.updateContent(newContent) },
                    label = { Text(stringResource(R.string.content_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(6.dp)
                        .weight(1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.place_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = uiState!!.placeName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(2f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable {
                            showDatePicker()
                        }
                ) {
                    Text(
                        text = stringResource(R.string.date_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = uiState!!.date,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(2f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable {
                            showTimePicker()
                        }
                ) {
                    Text(
                        text = stringResource(R.string.time_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = uiState!!.time,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(2f)
                    )
                }

                Button(
                    onClick = { viewModel.saveMeeting(uiState!!) },
                    enabled = !isButtonClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown, // brown color
                        contentColor = Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    )
    LaunchedEffect(uiState!!.isComplete) {
        if (uiState!!.isComplete) onNavigateUp()
    }
}

@Composable
fun showTimePicker(viewModel: MakeMeetingViewModel, isTimePickerState: MutableState<Boolean>) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay: Int, minute: Int ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        viewModel.updateTime(calendar.toCurrentTime())
        isTimePickerState.value = false
    }

    val timePicker = TimePickerDialog(
        context,
        timeSetListener,
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
    timePicker.setOnDismissListener {
        isTimePickerState.value = false
    }
    timePicker.setOnCancelListener {
        isTimePickerState.value = false
    }
    timePicker.show()
}
