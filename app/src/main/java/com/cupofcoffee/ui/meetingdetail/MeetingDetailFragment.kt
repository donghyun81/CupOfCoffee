package com.cupofcoffee.ui.meetingdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentMeetingDetailBinding
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar

class MeetingDetailFragment : Fragment() {

    private var _binding: FragmentMeetingDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeetingDetailViewModel by viewModels { MeetingDetailViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initUi() {
        viewModel.meetingDetailUiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    setMeeting(uiState.meeting)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setMeeting(meetingEntry: MeetingEntry) {
        with(binding) {
            tvContent.text = meetingEntry.meetingModel.content
            tvDate.text = meetingEntry.meetingModel.date
            tvPlace.text = meetingEntry.meetingModel.caption
            tvTime.text = meetingEntry.meetingModel.time
        }
    }
}