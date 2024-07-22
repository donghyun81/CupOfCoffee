package com.cupofcoffee.ui.commentdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cupofcoffee.R
import com.cupofcoffee.data.handle
import com.cupofcoffee.databinding.FragmentCommentEditBinding
import com.cupofcoffee.ui.model.CommentModel
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.showLoading
import com.cupofcoffee.ui.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.Date

class CommentEditFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommentEditViewModel by viewModels { CommentEditViewModel.Factory }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        viewModel.commentEditUiState.observe(viewLifecycleOwner) { result ->
            result.handle(
                onLoading = {
                    binding.cpiLoading.showLoading(result)
                },
                onSuccess = { uiState ->
                    binding.cpiLoading.showLoading(result)
                    setAddButtonClick(uiState.userEntry)
                    setUserProfile(uiState.userEntry.userModel.profileImageWebUrl)
                    val content = uiState.commentModel?.content ?: return@handle
                    setComment(content)
                },
                onError = {
                    binding.cpiLoading.showLoading(result)
                    view?.showSnackBar(R.string.data_error_message)
                }
            )

        }
    }

    private fun setAddButtonClick(userEntry: UserEntry) {
        with(binding) {
            btnAddComment.setOnClickListener {
                if (!viewModel.isNetworkConnected()) {
                    view?.showSnackBar(R.string.edit_comment_netwokr_message)
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    val comment = CommentModel(
                        userId = userEntry.id,
                        meetingId = viewModel.args.meetingId,
                        nickname = userEntry.userModel.nickname,
                        profileImageWebUrl = userEntry.userModel.profileImageWebUrl,
                        content = etComment.text.toString(),
                        createdDate = Date().time
                    )
                    if (viewModel.args.commentId == null) viewModel.insertComment(comment)
                    else viewModel.updateComment(comment)
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun setUserProfile(profileImageWebUrl: String?) {
        Glide.with(binding.root.context)
            .load(profileImageWebUrl)
            .centerCrop()
            .into(binding.ivUserProfile)
    }

    private fun setComment(context: String) {
        binding.etComment.setText(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}