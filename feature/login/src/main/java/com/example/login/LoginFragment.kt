package com.example.login

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.ui.graphics.Brown
import com.cupofcoffee0801.ui.graphics.Green
import com.example.common.component.StateContent
import com.example.common.graphics.AppTheme
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthBehavior
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    LoginScreen(
                        viewModel,
                        ::moveToHome
                    )
                }
            }
        }
    }

    private fun moveToHome() {
        val uri = Uri.parse("cupofcoffee://home")
        findNavController().navigate(uri)
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val isButtonClicked by viewModel.isButtonClicked.observeAsState(false)
    val uiState by viewModel.loginUiState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    val loginNetworkMessage = stringResource(id = R.string.login_network_message)

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = loginNetworkMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        StateContent(
            isError = uiState?.isError ?: false,
            isLoading = uiState?.isLoading ?: false,
            isComplete = uiState?.isComplete ?: false,
            navigateUp = onNavigateUp,
            data = uiState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brown)
                    .padding(8.dp),
            ) {

                Spacer(modifier = Modifier.height(150.dp))

                Image(
                    painter = painterResource(id = R.drawable.cup_of_coffee),
                    contentDescription = "앱 아이콘",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (viewModel.isNetworkConnected()) {
                            viewModel.switchButtonClicked()
                            NaverIdLoginSDK.behavior = NidOAuthBehavior.NAVERAPP
                            NaverIdLoginSDK.authenticate(context, object : OAuthLoginCallback {
                                override fun onSuccess() {
                                    viewModel.loginNaver()
                                }

                                override fun onFailure(httpStatus: Int, message: String) {
                                    viewModel.switchButtonClicked()
                                }

                                override fun onError(errorCode: Int, message: String) {
                                    viewModel.switchButtonClicked()
                                }
                            })
                        } else showSnackbar = true
                    },
                    enabled = !isButtonClicked,
                    colors = ButtonDefaults.buttonColors(Green),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.End)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.naver_logo),
                            contentDescription = "네이버 로고",
                            modifier = Modifier
                                .size(40.dp)
                                .padding(start = 20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.naver_login))
                    }
                }
            }
        }
    }
}