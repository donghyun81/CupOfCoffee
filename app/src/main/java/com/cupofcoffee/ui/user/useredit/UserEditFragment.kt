package com.cupofcoffee.ui.user.useredit

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentUserEditBinding
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import kotlinx.coroutines.launch

class UserEditFragment : DialogFragment() {

    private var _binding: FragmentUserEditBinding? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            setAddImage(isGranted)
        }
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            addImage(result)
        }

    private val binding get() = _binding!!
    private val viewModel: UserEditViewModel by viewModels { UserEditViewModel.Factory }

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
        setButtonEnable()
        setUserUi()
        setEditUserProfileOnclick()
        setEditProfileImage()
    }

    private fun setButtonEnable() {
        viewModel.isButtonClicked.observe(viewLifecycleOwner) { isButtonClicked ->
            binding.btnSave.isEnabled = !isButtonClicked
        }
    }

    private fun setEditUserProfileOnclick() {
        binding.ivProfileImage.setOnClickListener {
            requestAlbumAccessPermission()
        }
    }

    private fun setUserUi() {
        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val userModel = uiState.userEntry.userModel
                    with(binding) {
                        tvNickName.setText(userModel.nickname)
                        setUserProfile(userModel)
                    }
                    setSaveOnclick(uiState.userEntry, uiState.contentUri)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )
        }
    }

    private fun setEditProfileImage() {
        binding.ivProfileImage.setOnClickListener {
            requestAlbumAccessPermission()
        }
    }

    private fun setSaveOnclick(userEntry: UserEntry, contentUri: String?) {
        binding.btnSave.setOnClickListener {
            viewModel.onButtonClicked()
            val currentUserEntry = userEntry.copy(
                userModel = userEntry.userModel.copy(
                    nickname = binding.tvNickName.text.toString(),
                    profileImageWebUrl = contentUri
                )
            )
            if (viewModel.isNetworkConnected()) viewLifecycleOwner.lifecycleScope.launch {
                viewModel.updateUser(currentUserEntry)
                findNavController().navigateUp()
            }
            else view?.showSnackBar(R.string.network_profile)
        }
    }

    private fun requestAlbumAccessPermission() {
        val permissionId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissionLauncher.launch(permissionId)
    }

    private fun setAddImage(isGranted: Boolean) {
        if (isGranted) {
            pickImagesLauncher.launch(viewModel.getImagePick())
        }
    }

    private fun addImage(result: ActivityResult) {
        val contentUri: String = result.data?.data?.toString() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateUiState(contentUri)
            loadProfileImagePreview(contentUri)
        }
    }

    private fun loadProfileImagePreview(contentUri: String) {
        binding.ivProfileImage.run {
            Glide.with(context)
                .load(contentUri)
                .into(this)
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