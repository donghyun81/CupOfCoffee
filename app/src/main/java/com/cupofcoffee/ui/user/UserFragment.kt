package com.cupofcoffee.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.cupofcoffee.databinding.FragmentUserBinding
import com.cupofcoffee.ui.model.UserModel

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels { UserViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUserUi() {
        viewModel.user.observe(viewLifecycleOwner) { userEntry ->
            val userModel = userEntry.userModel
            setUserProfile(userModel)
            setUserNickName(userModel)
            setAttendedMeetingCount(userModel)
            setMakeMeetingCount(userModel)
        }
    }

    private fun setUserProfile(userModel: UserModel) {
        val profileUrl = userModel.profileImageWebUrl
        Glide.with(binding.root.context)
            .load(profileUrl)
            .centerCrop()
            .into(binding.ivProfile)
    }

    private fun setUserNickName(userModel: UserModel) {
        binding.tvNickName.text = userModel.nickname
    }

    private fun setAttendedMeetingCount(userModel: UserModel) {
        binding.tvAttendedMeetingCount.text = userModel.attendedMeetingIds.count().toString()
    }

    private fun setMakeMeetingCount(userModel: UserModel) {
        binding.tvMadeMeetingCount.text = userModel.madeMeetingIds.count().toString()
    }
}