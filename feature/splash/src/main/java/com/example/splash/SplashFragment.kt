package com.example.splash

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cupofcoffee0801.ui.graphics.Brown
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SplashScreen(
                    viewModel,
                    ::moveToHomeFragment,
                    ::moveToLoginFragment
                )
            }
        }
    }

    private fun moveToHomeFragment() {
        val uri = Uri.parse("cupofcoffee://home")
        findNavController().navigate(uri)
    }

    private fun moveToLoginFragment() {
        val uri = Uri.parse("cupofcoffee://login")
        findNavController().navigate(uri)
    }
}

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    moveToHome: () -> Unit,
    moveToLoginFragment: () -> Unit
) {
    val isAutoLoginState = viewModel.isAutoLoginFlow.observeAsState()

    LaunchedEffect(Unit) {
        delay(2000L)
        val isAutoLogin = isAutoLoginState.value!!
        val hasUser = Firebase.auth.uid != null
        if (isAutoLogin && hasUser && !viewModel.isUserDeleted()) moveToHome()
        else moveToLoginFragment()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brown)
            .padding(8.dp),
    ) {
        Spacer(modifier = Modifier.height(150.dp))
        Image(
            painterResource(id = R.drawable.cup_of_coffee),
            contentDescription = "splash 화면 아이콘",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}