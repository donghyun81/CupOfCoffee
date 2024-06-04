package com.cupofcoffee.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cupofcoffee.BuildConfig
import com.cupofcoffee.databinding.FragmentLoginBinding
import com.cupofcoffee.ui.model.NaverUser
import com.cupofcoffee.ui.model.toUserEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.launch

private const val NAVER_LOGIN_CLIENT_ID = BuildConfig.NAVER_LOGIN_CLIENT_ID
private const val NAVER_LOGIN_CLIENT_SECRET = BuildConfig.NAVER_LOGIN_CLIENT_SECRET
private const val APP_NAME = "CupOfCoffee"

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NaverIdLoginSDK.initialize(
            requireContext(), NAVER_LOGIN_CLIENT_ID,
            NAVER_LOGIN_CLIENT_SECRET,
            APP_NAME
        )
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNaverLogin()
    }

    private fun setNaverLogin() {
        binding.btnNaverLogin.setOAuthLogin(object : OAuthLoginCallback {
            override fun onSuccess() {
                loginNaver()
            }

            override fun onFailure(httpStatus: Int, message: String) {
            }

            override fun onError(errorCode: Int, message: String) {
            }
        })
    }

    fun loginNaver() {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                val naverUser = result.profile?.run {
                    NaverUser(
                        this.id!!,
                        name ?: "익명",
                        nickname,
                        profileImage
                    )
                } ?: return
                loginAccount(naverUser)
            }

            override fun onFailure(httpStatus: Int, message: String) {
            }

            override fun onError(errorCode: Int, message: String) {
            }
        })
    }

    fun loginAccount(naverUser: NaverUser) {
        with(naverUser) {
            auth.signInWithEmailAndPassword(id.toNaverEmail(), id)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        moveToHome()
                    } else {
                        Log.w("12345", task.exception)
                        createAccount(naverUser)
                    }
                }
        }
    }

    private fun createAccount(naverUser: NaverUser) {
        with(naverUser) {
            auth.createUserWithEmailAndPassword(id.toNaverEmail(), id)
                .addOnCompleteListener { task ->
                    require(task.isSuccessful) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val userEntry = naverUser.toUserEntry()
                            viewModel.insertUser(userEntry)
                            moveToHome()
                        }
                    }
                }
        }
    }

    private fun moveToHome() {
        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun String.toNaverEmail() = "${this.take(7)}@naver.com"
}