package com.cupofcoffee0801.ui.commentdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
    val content = rememberSaveable { mutableStateOf(uiState!!.comment?.content ?: "") }

    Row(modifier = Modifier.height(100.dp)) {
        if (uiState!!.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
        else {
            AsyncImage(
                model = uiState!!.user!!.profileImageWebUrl,
                contentDescription = "사용자 프로필",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop
            )
            TextField(
                value = content.value,
                onValueChange = { newContent -> content.value = newContent },
                label = { Text("내용") },
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = { viewModel.editComment(content = content.value) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }

    LaunchedEffect(uiState!!.isCompleted) {
        if (uiState!!.isCompleted) navigateUp()
    }
}