package com.example.meetingdetail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.example.common.component.OptionsMenu
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.example.common.toDateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetingDetailFragment : Fragment() {

    private val viewModel: MeetingDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    MeetingDetailScreen(
                        viewModel = viewModel,
                        meetingDetailNavigate = getMeetingDetailNavigate()
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.currentJob?.cancel()
    }

    private fun getMeetingDetailNavigate() = object : MeetingDetailNavigate {
        override fun navigateMakeMeeting(meetindId: String) {
            val uri =
                Uri.parse("cupofcoffee://make_meeting?placeName=${null}&lat=${null}&lng=${null}&meetingId=${meetindId}")
            findNavController().navigate(uri)
        }

        override fun navigateCommentEdit(commentId: String?, meetingId: String) {
            val uri =
                Uri.parse("cupofcoffee://comment_edit?commentId=${commentId}&meetingId=${meetingId}")
            findNavController().navigate(uri)
        }

        override fun navigateUp() {
            findNavController().navigateUp()
        }
    }
}

@Composable
fun MeetingDetailScreen(
    viewModel: MeetingDetailViewModel,
    meetingDetailNavigate: MeetingDetailNavigate
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.handleIntent(MeetingDetailIntent.HandleInitData)

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MeetingDetailSideEffect.NavigateToCommentEdit -> {
                    meetingDetailNavigate.navigateCommentEdit(effect.commentId, effect.meetingId)
                }

                is MeetingDetailSideEffect.NavigateToMakeMeeting -> {
                    meetingDetailNavigate.navigateMakeMeeting(effect.meetingId)
                }

                is MeetingDetailSideEffect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    StateContent(
        isError = uiState.isError,
        isLoading = uiState.isLoading,
        data = uiState
    ) { data ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.isMyMeeting)
                OptionsMenu(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp),
                    onEditClick = { viewModel.handleIntent(MeetingDetailIntent.EditMeeting(data.meetingUiModel.id)) },
                    onDeleteClick = { viewModel.handleIntent(MeetingDetailIntent.DeleteMeeting) }
                )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(8.dp),
                text = data.meetingUiModel.content,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            MeetingInfo(
                label = stringResource(id = R.string.place_label),
                value = data.meetingUiModel.caption
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            MeetingInfo(
                label = stringResource(id = R.string.date_label),
                value = data.meetingUiModel.date
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            MeetingInfo(
                label = stringResource(id = R.string.time_label),
                value = data.meetingUiModel.time
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Comments(
                comments = data.comments,
                navigate = meetingDetailNavigate,
                modifier = Modifier.weight(1f),
                viewModel = viewModel
            )

            CommentInput(
                navigate = meetingDetailNavigate,
                meetingId = data.meetingUiModel.id,
                userProfileUrl = data.userUiModel.profileUrl
            )
        }
    }
}

@Composable
fun MeetingInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Comments(
    comments: List<MeetingDetailCommentUiModel>,
    navigate: MeetingDetailNavigate,
    viewModel: MeetingDetailViewModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "댓글",
            fontSize = 26.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            items(comments) { comment ->
                CommentItem(commentUiModel = comment, viewModel = viewModel, navigate = navigate)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CommentInput(
    navigate: MeetingDetailNavigate,
    meetingId: String,
    userProfileUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userProfileUrl,
            contentDescription = "User profile image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "댓글 작성",
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clickable { navigate.navigateCommentEdit(null, meetingId = meetingId) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CommentItem(
    commentUiModel: MeetingDetailCommentUiModel,
    viewModel: MeetingDetailViewModel,
    navigate: MeetingDetailNavigate,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = commentUiModel.profileImageWebUrl,
            contentDescription = "사용자 프로필",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = commentUiModel.nickname ?: "익명",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .wrapContentSize(align = Alignment.CenterStart),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = commentUiModel.createdDate.toDateFormat(),
                    modifier = Modifier
                        .wrapContentWidth(Alignment.End)
                        .padding(end = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = commentUiModel.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (commentUiModel.isMyComment)
            CommentOptionsMenu(
                commentId = commentUiModel.id,
                meetingId = commentUiModel.id,
                navigate = navigate,
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
    }
}

@Composable
fun CommentOptionsMenu(
    commentId: String,
    meetingId: String,
    viewModel: MeetingDetailViewModel,
    navigate: MeetingDetailNavigate,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = modifier
            .size(24.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
    ) {
        Icon(
            painterResource(id = R.drawable.baseline_more_vert_24),
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "수정",
                    modifier = Modifier
                        .clickable {
                            navigate.navigateCommentEdit(commentId, meetingId)
                            expanded = false
                        }
                        .padding(8.dp)
                )
                Text(
                    text = "삭제",
                    modifier = Modifier
                        .clickable {
                            viewModel.handleIntent(MeetingDetailIntent.DeleteComment(commentId))
                            expanded = false
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}