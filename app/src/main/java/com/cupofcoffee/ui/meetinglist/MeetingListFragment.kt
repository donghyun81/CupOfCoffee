package com.cupofcoffee.ui.meetinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cupofcoffee.databinding.FragmentMeetingListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MeetingListFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMeetingListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeetingListBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}