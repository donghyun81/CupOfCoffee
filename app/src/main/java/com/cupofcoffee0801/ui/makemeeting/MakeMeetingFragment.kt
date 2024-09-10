package com.cupofcoffee0801.ui.makemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.component.StateContent
import com.cupofcoffee0801.ui.graphics.AppTheme
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
                AppTheme {
                    MakeMeetingScreen(
                        viewModel,
                        ::showMeetingDatePicker,
                        ::showMeetingTimePicker,
                        ::navigateUp,
                    )
                }
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
    val isButtonClicked by remember { mutableStateOf(true) }

    StateContent(
        isError = uiState?.isError ?: false,
        isLoading = uiState?.isLoading ?: false,
        isComplete = uiState?.isComplete ?: false,
        navigateUp = onNavigateUp,
        data = uiState
    ) { data ->
        Scaffold(
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = data!!.content,
                        onValueChange = { newContent -> viewModel.updateContent(newContent) },
                        label = { Text(stringResource(R.string.content_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.place_label),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = data.placeName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(2f)
                                .wrapContentSize(align = Alignment.CenterEnd)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.date_label),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            text = data.date,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(2f)
                                .wrapContentSize(align = Alignment.CenterEnd)
                                .clickable { showDatePicker() }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.time_label),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = data.time,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(2f)
                                .wrapContentSize(align = Alignment.CenterEnd)
                                .clickable {
                                    showTimePicker()
                                }
                        )
                    }

                    Button(
                        onClick = { viewModel.saveMeeting(data) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        )
    }
}