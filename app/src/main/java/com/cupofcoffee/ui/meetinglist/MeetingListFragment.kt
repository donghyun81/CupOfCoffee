package com.cupofcoffee.ui.meetinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.databinding.FragmentMeetingListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class MeetingListFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMeetingListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MeetingListViewModel by viewModels { MeetingListViewModel.Factory }

    private val adapter: MeetingListAdapter = MeetingListAdapter(applyOnclick())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initTitle()
        setCancelButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initTitle() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            val placeEntry = uiState.placeEntry ?: return@observe
            binding.tvTitle.text = placeEntry.placeModel.caption
        }
    }

    private fun initAdapter() {
        binding.rvMeetings.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            adapter.submitList(uiState.meetingEntriesWithPeople)
        }
    }

    private fun applyOnclick() = object : MeetingClickListener {

        override fun onClick(meetingEntryWithPeople: MeetingEntryWithPeople) {
            viewModel.applyMeeting(meetingEntryWithPeople)
        }
    }

    private fun setCancelButton() {
        binding.ibCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}