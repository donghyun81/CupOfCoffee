package com.cupofcoffee.ui.meetinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentMeetingListBinding
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
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
        initUi()
        setCancelButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initUi() {
        binding.rvMeetings.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    setTitle(uiState.placeEntry)
                    setAdapter(uiState.meetingEntriesWithPeople)
                },
                onError = {
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setTitle(placeEntry: PlaceEntry) {
        binding.tvTitle.text = placeEntry.placeModel.caption
    }

    private fun setAdapter(meetingEntriesWithPeople: List<MeetingEntryWithPeople>) {
        binding.rvMeetings.adapter = adapter
        adapter.submitList(meetingEntriesWithPeople)
    }

    private fun applyOnclick() = object : MeetingClickListener {

        override fun onClick(meetingEntryWithPeople: MeetingEntryWithPeople) {
            if (viewModel.isNetworkConnected()) viewModel.applyMeeting(meetingEntryWithPeople)
            else view?.showSnackBar(R.string.attended_network_message)
        }
    }

    private fun setCancelButton() {
        binding.ibCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}