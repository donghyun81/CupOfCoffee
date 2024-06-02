package com.cupofcoffee.ui.savemeeting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cupofcoffee.databinding.FragmentSaveMeetingBinding
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.internal.toLongOrDefault
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class SaveMeetingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSaveMeetingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SaveMeetingViewModel by viewModels { SaveMeetingViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSaveMeetingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPlace()
        setMeetingTime()
        setSaveButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setPlace() {
        binding.tvPlace.text = viewModel.args.placeName
    }

    private fun setMeetingTime() {
        val calendar = Calendar.getInstance()
        binding.tvTime.text = SimpleDateFormat("HH:mm").format(calendar.time)
        binding.tvTime.setOnClickListener {
            showMeetingTimePicker(calendar)
        }
    }

    private fun showMeetingTimePicker(calendar: Calendar) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            binding.tvTime.text = SimpleDateFormat("HH:mm").format(calendar.time)
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
                    peopleId = mutableListOf(),
                    time = tvTime.text.toString().toLong(),
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