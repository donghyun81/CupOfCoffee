package com.cupofcoffee0801

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cupofcoffee0801.data.module.AuthTokenManager
import com.cupofcoffee0801.data.module.RepositoryModule
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.data.worker.SyncLocalWorker
import com.cupofcoffee0801.util.NetworkUtil
import com.navercorp.nid.NaverIdLoginSDK

private const val NAVER_LOGIN_CLIENT_ID = BuildConfig.NAVER_LOGIN_CLIENT_ID
private const val NAVER_LOGIN_CLIENT_SECRET = BuildConfig.NAVER_LOGIN_CLIENT_SECRET
private const val APP_NAME = "CupOfCoffee"

class CupOfCoffeeApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        initNaverLogin()
        AuthTokenManager.initializeAuthListener()
        RepositoryModule.initDatabase(applicationContext)

        networkUtil = NetworkUtil(applicationContext)

        preferencesRepositoryImpl = RepositoryModule.getPreferencesRepository(applicationContext)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val localWorker = OneTimeWorkRequestBuilder<SyncLocalWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .beginWith(localWorker)
            .enqueue()
    }

    private fun initNaverLogin() {
        NaverIdLoginSDK.initialize(
            applicationContext, NAVER_LOGIN_CLIENT_ID,
            NAVER_LOGIN_CLIENT_SECRET,
            APP_NAME
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        AuthTokenManager.removeAuthListener()
    }

    companion object {

        val meetingRepository: MeetingRepositoryImpl by lazy {
            RepositoryModule.getMeetingRepository()
        }

        val placeRepository: PlaceRepositoryImpl by lazy {
            RepositoryModule.getPlaceRepository()
        }

        val userRepository: UserRepositoryImpl by lazy {
            RepositoryModule.getUserRepository()
        }

        val commentRepository: CommentRepositoryImpl by lazy {
            RepositoryModule.getCommentRepository()
        }

        lateinit var preferencesRepositoryImpl: PreferencesRepositoryImpl

        lateinit var networkUtil: NetworkUtil
    }
}