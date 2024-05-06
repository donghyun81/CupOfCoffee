package com.cupofcoffee

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.cupofcoffee.databinding.FragmentSaveMeetingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import java.text.SimpleDateFormat
import java.util.Calendar

class SaveMeetingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSaveMeetingBinding? = null
    private val binding get() = _binding!!

    private val args: SaveMeetingFragmentArgs by navArgs()

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
        setTime()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setPlace() {
        binding.tvPlace.text = args.placeName
    }

    private fun setTime() {
        val cal = Calendar.getInstance()
        binding.tvTime.text = SimpleDateFormat("HH:mm").format(cal.time)
        binding.tvTime.setOnClickListener {
            showTimePicker(cal)
        }
    }

    private fun showTimePicker(calendar: Calendar) {
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
}