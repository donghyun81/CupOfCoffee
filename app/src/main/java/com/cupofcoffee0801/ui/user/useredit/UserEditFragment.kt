package com.cupofcoffee0801.ui.user.useredit

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.handle
import com.cupofcoffee0801.databinding.FragmentUserEditBinding
import com.cupofcoffee0801.ui.model.User
import com.cupofcoffee0801.ui.model.UserData
import com.cupofcoffee0801.ui.showLoading
import com.cupofcoffee0801.ui.showSnackBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class UserEditFragment : DialogFragment() {

    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserEditViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            setAddImage(isGranted)
        }
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            addImage(uri)
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        viewModel.dataResult.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = { binding.cpiLoading.showLoading(result) },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    val user = uiState.user
                    with(binding) {
                        tvNickName.setText(user.nickname)
                        setUserProfile(user.profileImageWebUrl)
                    }
                    setSaveOnclick(uiState.user, uiState.contentUri)
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

    private fun setSaveOnclick(user: User, contentUri: String?) {
        binding.btnSave.setOnClickListener {
            viewModel.onButtonClicked()
            viewLifecycleOwner.lifecycleScope.launch {
                val currentUserEntry = getCurrentUser(user, contentUri)
                delay(2000L)
                if (viewModel.isNetworkConnected()) {
                    viewModel.updateUser(currentUserEntry)
                    viewModel.updateUserComments(currentUserEntry)
                    findNavController().navigateUp()
                } else view?.showSnackBar(R.string.network_profile)
            }
        }
    }

    private suspend fun getCurrentUser(user: User, contentUri: String?): User {
        val uid = Firebase.auth.uid!!
        val storageReference = FirebaseStorage.getInstance().reference
        val ref = storageReference.child("images/$uid")
        val imageUri = Uri.parse(contentUri)
        return try {
            ref.putFile(imageUri).await()
            val uri = ref.downloadUrl.await()
            user.copy(
                nickname = binding.tvNickName.text.toString(),
                profileImageWebUrl = uri.toString()
            )
        } catch (e: Exception) {
            user
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
            pickImagesLauncher.launch("image/*")
        }
    }

    private fun addImage(uri: Uri?) {
        val contentUri: String = uri?.toString() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateUiState(contentUri)
            loadProfileImagePreview(contentUri)
        }
    }

    private fun loadProfileImagePreview(contentUri: String) {
        val uri = Uri.parse(contentUri)
        binding.ivProfileImage.run {
            Glide.with(context)
                .load(uri)
                .into(this)
        }
    }

    private fun setUserProfile(profileImageWebUrl: String?) {
        Glide.with(binding.root.context)
            .load(profileImageWebUrl)
            .centerCrop()
            .into(binding.ivProfileImage)
    }
}