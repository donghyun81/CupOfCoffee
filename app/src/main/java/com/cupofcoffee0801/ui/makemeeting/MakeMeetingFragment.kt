package com.cupofcoffee0801.ui.makemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.databinding.FragmentMakeMeetingBinding
import com.cupofcoffee0801.ui.isCurrentDateOver
import com.cupofcoffee0801.ui.model.MeetingModel
import com.cupofcoffee0801.ui.model.PlaceModel
import com.cupofcoffee0801.ui.showLoading
import com.cupofcoffee0801.ui.showSnackBar
import com.cupofcoffee0801.ui.toCurrentDate
import com.cupofcoffee0801.ui.toCurrentTime
import com.cupofcoffee0801.ui.toDateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class MakeMeetingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMakeMeetingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MakeMeetingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMakeMeetingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonEnable()
        setUi()
        setMeetingTime()
        setMeetingDate()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setButtonEnable() {
        viewModel.isButtonClicked.observe(viewLifecycleOwner) { isButtonClicked ->
            binding.btnSave.isEnabled = !isButtonClicked
        }
    }

    private fun setUi() {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    if (uiState.meetingEntry != null) {
                        binding.tvPlace.text = uiState.placeName
                        binding.tvTime.text = uiState.meetingEntry.meetingModel.time
                        binding.tvDate.text = uiState.meetingEntry.meetingModel.date
                        binding.tvContent.setText(uiState.meetingEntry.meetingModel.content)
                    } else binding.tvPlace.text = uiState.placeName
                    setSaveButton(uiState.placeName, uiState.lat, uiState.lng)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )

        }
        binding.tvPlace.text = viewModel.args.placeName
    }

    private fun setMeetingDate() {
        val calendar = Calendar.getInstance()
        binding.tvDate.text = calendar.toCurrentDate()
        binding.tvDate.setOnClickListener {
            showMeetingDatePicker()
        }
    }

    private fun showMeetingDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(
            getString(R.string.date_picker_title)
        )
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
        datePicker.show(parentFragmentManager, getString(R.string.meeting_date_picker_tag))
        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            if (selectedDateInMillis.isCurrentDateOver()) binding.tvDate.text =
                selectedDateInMillis.toDateFormat()
            else view?.showSnackBar(R.string.select_previous_date)
        }
    }

    private fun setMeetingTime() {
        val calendar = Calendar.getInstance()
        binding.tvTime.text = calendar.toCurrentTime()
        binding.tvTime.setOnClickListener {
            showMeetingTimePicker(calendar)
        }
    }

    private fun showMeetingTimePicker(calendar: Calendar) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            binding.tvTime.text = calendar.toCurrentTime()
        }
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setSaveButton(placeName: String, lat: Double, lng: Double) {
        binding.btnSave.setOnClickListener {
            viewModel.onButtonClicked()
            val uid = Firebase.auth.uid!!
            with(binding) {
                val meeting = MeetingModel(
                    caption = placeName,
                    lat = lat,
                    lng = lng,
                    managerId = uid,
                    personIds = mutableMapOf(uid to true),
                    placeId = viewModel.convertPlaceId(lat, lng),
                    date = tvDate.text.toString(),
                    time = tvTime.text.toString(),
                    createDate = Date().time,
                    content = tvContent.text.toString()
                )
                val placeModel = PlaceModel(
                    caption = placeName,
                    lat = lat,
                    lng = lng,
                )
                if (viewModel.isNetworkConnected()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.saveMeeting(meeting, placeModel)
                        findNavController().navigateUp()
                    }
                } else view?.showSnackBar(R.string.disconnect_network_message)
            }
        }
    }
}