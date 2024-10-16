package com.example.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.example.common.component.OptionsMenu
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.example.model.MeetingsCategory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    UserScreen(
                        viewModel = viewModel,
                        userNavigate = getUserNavigate()
                    )
                }
            }
        }
    }

    private fun getUserNavigate() = object : UserNavigate {

        override fun navigateMeetingDetail(meetingId: String) {
            val uri = Uri.parse("cupofcoffee://meeting_detail?meetingId=${meetingId}")
            findNavController().navigate(uri)
        }

        override fun navigateMakeMeeting(meetingId: String) {
            val uri =
                Uri.parse("cupofcoffee://make_meeting?placeName=${null}&lat=${null}&lng=${null}&meetingId=${meetingId}")
            findNavController().navigate(uri)
        }

        override fun navigateSettings() {
            val uri = Uri.parse("cupofcoffee://settings")
            findNavController().navigate(uri)
        }

        override fun navigateUserEdit() {
            val uri = Uri.parse("cupofcoffee://user_edit")
            findNavController().navigate(uri)
        }
    }
}

@Composable
fun UserScreen(
    viewModel: UserViewModel,
    userNavigate: UserNavigate
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pages = MeetingsCategory.tabCategories
    val pagerState = rememberPagerState { pages.size }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(intent = UserIntent.InitData)
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is UserSideEffect.NavigateMakeMeeting -> {
                    userNavigate.navigateMakeMeeting(effect.meetingId)
                }

                is UserSideEffect.NavigateMeetingDetail -> {
                    userNavigate.navigateMeetingDetail(effect.meetingId)
                }

                UserSideEffect.NavigateSettings -> {
                    userNavigate.navigateSettings()
                }

                UserSideEffect.NavigateUserEdit -> {
                    userNavigate.navigateUserEdit()
                }
            }
        }
    }

    StateContent(
        isError = uiState.isError,
        isLoading = uiState.isLoading,
        data = uiState
    ) { data ->
        UserContent(
            viewModel = viewModel,
            uiState = data,
            pagerState = pagerState,
            pages = pages,
            coroutineScope = coroutineScope
        )
    }
}

@Composable
fun UserContent(
    viewModel: UserViewModel,
    uiState: UserUiState,
    pagerState: PagerState,
    pages: List<MeetingsCategory>,
    coroutineScope: CoroutineScope
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsIcon(Modifier.align(Alignment.End), viewModel = viewModel)
        UserProfileCard(uiState = uiState, viewModel = viewModel)
        UserTabs(pagerState = pagerState, pages = pages, coroutineScope = coroutineScope)
        UserPager(
            pagerState = pagerState,
            pages = pages,
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
fun SettingsIcon(
    modifier: Modifier,
    viewModel: UserViewModel
) {
    IconButton(
        onClick = { viewModel.handleIntent(UserIntent.SettingClick) },
        modifier = modifier
            .padding(top = 8.dp, end = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_settings_24),
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun UserProfileCard(viewModel: UserViewModel, uiState: UserUiState) {
    Card(
        modifier = Modifier
            .padding(top = 30.dp)
            .width(360.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = uiState.profileUrl,
                contentDescription = "사용자 프로필",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = uiState.nickName ?: "익명",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                MeetingStats(
                    labelRes = R.string.made_meeting_count,
                    count = uiState.madeMeetings.count()
                )
                MeetingStats(
                    labelRes = R.string.attended_meeting_count,
                    count = uiState.attendedMeetings.count()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { viewModel.handleIntent(UserIntent.UserEditClick) }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_24),
                    contentDescription = "프로필 수정"
                )
            }
        }
    }
}

@Composable
fun MeetingStats(@StringRes labelRes: Int, count: Int) {
    Row {
        Text(text = stringResource(id = labelRes))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = count.toString())
    }
}

@Composable
fun UserTabs(
    pagerState: PagerState,
    pages: List<MeetingsCategory>,
    coroutineScope: CoroutineScope
) {
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 0.dp
    ) {
        pages.forEachIndexed { index, page ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(text = stringResource(id = page.stringResourceId))
                }
            )
        }
    }
}

@Composable
fun UserPager(
    viewModel: UserViewModel,
    pagerState: PagerState,
    pages: List<MeetingsCategory>,
    uiState: UserUiState,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
    ) { page ->
        val pageData = pages[page]
        when (pageData) {
            MeetingsCategory.MADE_MEETINGS ->
                MeetingPage(
                    viewModel,
                    userMeetings = uiState.madeMeetings,
                    isMadeMeeting = true
                )

            MeetingsCategory.ATTENDED_MEETINGS ->
                MeetingPage(
                    viewModel,
                    userMeetings = uiState.attendedMeetings,
                )
        }
    }
}

@Composable
fun MeetingPage(
    viewModel: UserViewModel,
    userMeetings: List<UserMeeting>,
    isMadeMeeting: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            MeetingHeader()
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(userMeetings) { meeting ->
                    MeetingItem(
                        viewModel = viewModel,
                        userMeeting = meeting,
                        isMadeMeeting = isMadeMeeting
                    )
                }
            }
        }
    }
}

@Composable
fun MeetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeetingHeaderText(stringResource(id = R.string.date_label), Modifier.weight(1f))
        MeetingHeaderText(stringResource(id = R.string.place_label), Modifier.weight(1.5f))
        MeetingHeaderText(stringResource(id = R.string.meeting_content_label), Modifier.weight(2f))
    }
}

@Composable
fun MeetingHeaderText(text: String, modifier: Modifier) {
    Text(
        text = text,
        fontSize = 24.sp,
        modifier = modifier.padding(4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun MeetingItem(
    viewModel: UserViewModel,
    userMeeting: UserMeeting,
    isMadeMeeting: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(80.dp)
            .clickable { viewModel.handleIntent(UserIntent.DetailMeetingClick(userMeeting.id)) },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MeetingDate(
            date = userMeeting.date,
            time = userMeeting.time,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = userMeeting.place,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.CenterVertically)
                .weight(1.5f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )

        Text(
            text = userMeeting.content,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp)
                .align(Alignment.CenterVertically)
                .weight(2f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (isMadeMeeting)
            OptionsMenu(
                modifier = Modifier.align(Alignment.CenterVertically),
                onEditClick = { viewModel.handleIntent(UserIntent.UpdateMeetingClick(userMeeting.id)) },
                onDeleteClick = { viewModel.handleIntent(UserIntent.DeleteMeetingClick(userMeeting.id)) }
            )
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

@Composable
fun MeetingDate(date: String, time: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(end = 8.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )

        Text(
            text = time,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}