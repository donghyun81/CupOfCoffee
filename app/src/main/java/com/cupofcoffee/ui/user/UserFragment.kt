package com.cupofcoffee.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentUserBinding
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.UserModel
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import com.cupofcoffee.ui.user.usermettings.UserMeetingsPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels { UserViewModel.Factory }

    private lateinit var userMeetingsPagerAdapter: UserMeetingsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserUi()
        setMeetingsAdapter()
        setSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setSettings() {
        binding.ivSettings.setOnClickListener {
            val action = UserFragmentDirections.actionUserFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun setUserUi() {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val userModel = uiState.user?.userModel ?: return@observe
                    setUserProfile(userModel)
                    with(binding) {
                        tvNickName.text = userModel.nickname
                        tvAttendedMeetingCount.text = uiState.attendedMeetingsCount.toString()
                        binding.tvMadeMeetingCount.text = uiState.madeMeetingsCount.toString()
                    }
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setUserProfile(userModel: UserModel) {
        val profileUrl = userModel.profileImageWebUrl
        Glide.with(binding.root.context)
            .load(profileUrl)
            .centerCrop()
            .into(binding.ivProfile)
    }

    private fun setMeetingsAdapter() {
        with(binding) {
            val tabCategories = MeetingsCategory.tabCategories
            userMeetingsPagerAdapter = UserMeetingsPagerAdapter(this@UserFragment)
            vpMeetings.adapter = userMeetingsPagerAdapter
            TabLayoutMediator(tlCategory, vpMeetings) { tab, position ->
                tab.text = getString(tabCategories[position].stringResourceId)
            }.attach()
        }
    }
}