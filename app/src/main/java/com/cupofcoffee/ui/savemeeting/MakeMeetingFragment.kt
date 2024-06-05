package com.cupofcoffee.ui.savemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.databinding.FragmentMakeMeetingBinding
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MEETING_DATE_PICKER_TITLE = "모임 날짜 선택"
private const val MEETING_DATE_PICKER_TAG = "meeting_date"
private const val MEETING_DATE_FORMAT = "yyyy-MM-dd"
private const val MEETING_TIME_FORMAT = "HH:mm"

class MakeMeetingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMakeMeetingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MakeMeetingViewModel by viewModels { MakeMeetingViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMakeMeetingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPlace()
        setMeetingTime()
        setSaveButton()
        setMeetingDate()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setPlace() {
        binding.tvPlace.text = viewModel.args.placeName
    }

    private fun setMeetingDate() {
        val calendar = Calendar.getInstance()
        binding.tvDate.text = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
        binding.tvDate.setOnClickListener {
            showMeetingDatePicker()
        }
    }

    private fun showMeetingDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(
            MEETING_DATE_PICKER_TITLE
        )
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
        datePicker.show(parentFragmentManager, MEETING_DATE_PICKER_TAG)
        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            binding.tvDate.text = selectedDateInMillis.toDateFormat()
        }
    }

    private fun Long.toDateFormat(): String {
        val date = Date(this)
        val format = SimpleDateFormat(MEETING_DATE_FORMAT, Locale.getDefault())
        return format.format(date)
    }

    private fun setMeetingTime() {
        val calendar = Calendar.getInstance()
        binding.tvTime.text = calendar.toCurrentTime()
        binding.tvTime.setOnClickListener {
            showMeetingTimePicker(calendar)
        }
    }

    private fun Calendar.toCurrentTime(): String =
        SimpleDateFormat(MEETING_TIME_FORMAT).format(this.time)


    private fun showMeetingTimePicker(calendar: Calendar) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
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


    private fun setSaveButton() {
        binding.btnSave.setOnClickListener {
            with(binding) {
                val meeting = MeetingModel(
                    caption = viewModel.args.placeName,
                    lat = viewModel.args.placePosition.latitude,
                    lng = viewModel.args.placePosition.longitude,
                    managerId = Firebase.auth.uid!!,
                    peopleId = mutableListOf(Firebase.auth.uid!!),
                    date = tvDate.text.toString(),
                    time = tvTime.text.toString(),
                    createDate = Date().time,
                    content = tvContent.text.toString()
                )
                val placeModel = PlaceModel(
                    caption = viewModel.args.placeName,
                    lat = viewModel.args.placePosition.latitude,
                    lng = viewModel.args.placePosition.longitude,
                )
                viewModel.saveMeeting(meeting, placeModel)
            }
        }
    }
}