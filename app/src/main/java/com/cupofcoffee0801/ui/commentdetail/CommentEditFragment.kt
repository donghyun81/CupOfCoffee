package com.cupofcoffee0801.ui.commentdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.component.StateContent
import com.cupofcoffee0801.ui.graphics.AppTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentEditFragment : BottomSheetDialogFragment() {

    private val viewModel: CommentEditViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CommentEditScreen(
                        viewModel = viewModel,
                        navigateUp = ::navigateUp
                    )
                }
            }
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

@Composable
fun CommentEditScreen(
    viewModel: CommentEditViewModel,
    navigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.observeAsState()
    var content = remember { mutableStateOf(uiState!!.comment?.content ?: "") }
    var isButtonClicked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    val editCommentNetworkMessage = stringResource(id = R.string.edit_comment_netwokr_message)

    LaunchedEffect(uiState!!.comment?.content) {
        content.value = uiState!!.comment?.content ?: ""
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = editCommentNetworkMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    StateContent(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        isError = uiState?.isError ?: false,
        isLoading = uiState?.isLoading ?: false,
        isComplete = uiState?.isCompleted ?: false,
        navigateUp = navigateUp,
        data = uiState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = uiState!!.user!!.profileImageWebUrl,
                    contentDescription = "사용자 프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                TextField(
                    value = content.value ,
                    onValueChange = { newContent -> content.value = newContent },
                    label = { Text("내용") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                        .align(Alignment.CenterVertically)
                )

                Button(
                    onClick = {
                        if (viewModel.isNetworkConnected()) {
                            isButtonClicked = true
                            viewModel.editComment(content = content.value)
                        } else showSnackbar = true
                    },
                    enabled = !isButtonClicked,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}