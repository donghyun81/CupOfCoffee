package com.cupofcoffee.ui.meetinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentMeetingListBinding
import com.cupofcoffee.ui.showLoading
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = {
                    binding.cpiLoading.showLoading(result)
                },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val placeEntry = uiState.placeEntry ?: return@observe
                    binding.tvTitle.text = placeEntry.placeModel.caption
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                }
            )
        }
    }

    private fun initAdapter() {
        binding.rvMeetings.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = {},
                onSuccess = { uiState ->
                    adapter.submitList(uiState.meetingEntriesWithPeople)
                },
                onError = {}
            )
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