package com.cupofcoffee0801.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.R
import com.cupofcoffee0801.ui.graphics.Brown
import com.cupofcoffee0801.ui.graphics.Green
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
                LoginScreen(
                    viewModel,
                    ::moveToHome
                )
            }
        }
    }

    private fun moveToHome() {
        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    moveToHome: () -> Unit
) {
    val context = LocalContext.current
    val isButtonClicked by viewModel.isButtonClicked.observeAsState(false)
    val loginState by viewModel.loginState.observeAsState(LoginState.Idle)
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Brown) // Background color
            .padding(16.dp)
    ) {
        // Central Image (App Logo)
        Image(
            painter = painterResource(id = R.drawable.cup_of_coffee),
            contentDescription = "앱 아이콘",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
        )

        // Naver Login Button with Naver Logo
        Button(
            onClick = {
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
            },
            enabled = !isButtonClicked,
            colors = ButtonDefaults.buttonColors(Green),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
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
                Spacer(modifier = Modifier.width(8.dp)) // Space between logo and text
                Text(text = stringResource(id = R.string.naver_login))
            }
        }

        when (loginState) {
            is LoginState.Error -> {
                val errorMessage = (loginState as LoginState.Error).message
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.clearError()
                }
            }

            LoginState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LoginState.Success -> {
                LaunchedEffect(Unit) {
                    moveToHome()
                }
            }

            LoginState.Idle -> Unit
        }
    }
}
