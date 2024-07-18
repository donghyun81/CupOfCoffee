package com.cupofcoffee.ui.user.useredit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentUserBinding
import com.cupofcoffee.databinding.FragmentUserEditBinding
import com.cupofcoffee.ui.model.UserModel
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import com.cupofcoffee.ui.user.UserViewModel

class UserEditFragment : DialogFragment() {

    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels { UserEditViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserUi()
    }

    private fun setUserUi() {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val userModel = uiState.user?.userModel ?: return@observe
                    with(binding) {
                        tvNickName.setText(userModel.nickname)
                        setUserProfile(userModel)
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
            .into(binding.ivProfileImage)
    }
}