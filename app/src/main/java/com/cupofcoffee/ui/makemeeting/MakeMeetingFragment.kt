package com.cupofcoffee.ui.makemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cupofcoffee.R
import com.cupofcoffee.databinding.FragmentMakeMeetingBinding
import com.cupofcoffee.ui.isCurrentDateOver
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.showSnackBar
import com.cupofcoffee.ui.toCurrentDate
import com.cupofcoffee.ui.toCurrentTime
import com.cupofcoffee.ui.toDateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

class MakeMeetingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMakeMeetingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MakeMeetingViewModel by viewModels { MakeMeetingViewModel.Factory }

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


    private fun setSaveButton() {
        binding.btnSave.setOnClickListener {
            val uid = Firebase.auth.uid!!
            with(binding) {
                val meeting = MeetingModel(
                    caption = viewModel.args.placeName,
                    lat = viewModel.args.placePosition.latitude,
                    lng = viewModel.args.placePosition.longitude,
                    managerId = uid,
                    personIds = mutableMapOf(uid to true),
                    placeId = viewModel.convertPlaceId(),
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