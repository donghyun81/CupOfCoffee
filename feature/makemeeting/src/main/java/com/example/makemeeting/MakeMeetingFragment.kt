package com.example.makemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.example.common.isCurrentDateOver
import com.example.common.showSnackBar
import com.example.common.toCurrentTime
import com.example.common.toDateFormat
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
            if (selectedDateInMillis.isCurrentDateOver()) viewModel.handleIntent(
                MakeMeetingIntent.EditDate(
                    selectedDateInMillis.toDateFormat()
                )
            )
            else view?.showSnackBar(R.string.select_previous_date)
        }
    }

    private fun showMeetingTimePicker() {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.handleIntent(MakeMeetingIntent.EditTime(calendar.toCurrentTime()))
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
    viewModel: MakeMeetingViewModel,
    showDatePicker: () -> Unit,
    showTimePicker: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.handleIntent(MakeMeetingIntent.InitData)

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MakeMeetingSideEffect.NavigateUp -> {
                    onNavigateUp()
                }
                is MakeMeetingSideEffect.ShowSnackBar ->
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
            }
        }
    }
    StateContent(
        isError = uiState.isError,
        isLoading = uiState.isLoading,
        navigateUp = onNavigateUp,
        data = uiState
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues)
                            .padding(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.meetingData.content,
                            onValueChange = { newContent ->
                                viewModel.handleIntent(
                                    intent = MakeMeetingIntent.EnterContent(
                                        newContent
                                    )
                                )
                            },
                            label = { Text(stringResource(R.string.content_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(6.dp)
                        )

                        InfoRow(
                            label = stringResource(R.string.place_label),
                            content = uiState.meetingData.placeName
                        )

                        InfoRow(
                            label = stringResource(R.string.date_label),
                            content = uiState.meetingData.date,
                            onClick = showDatePicker
                        )

                        InfoRow(
                            label = stringResource(R.string.time_label),
                            content = uiState.meetingData.time,
                            onClick = showTimePicker
                        )

                        Button(
                            onClick = { viewModel.handleIntent(MakeMeetingIntent.MakeMeeting) },
                            enabled = uiState.isSaveButtonEnable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(text = stringResource(R.string.save))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, content: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(2f)
                .wrapContentSize(align = Alignment.CenterEnd)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
        )
    }
}