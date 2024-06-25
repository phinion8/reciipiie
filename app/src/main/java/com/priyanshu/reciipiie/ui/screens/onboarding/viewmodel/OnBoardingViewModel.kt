package com.priyanshu.reciipiie.ui.screens.onboarding.viewmodel

import android.content.Context
import android.content.IntentSender
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.priyanshu.reciipiie.domain.repository.PreferenceManager
import com.priyanshu.reciipiie.domain.usecases.OnBoardingUseCase
import com.priyanshu.reciipiie.utils.Constants
import com.priyanshu.reciipiie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val onBoardingUseCase: OnBoardingUseCase
) : ViewModel() {

    private val _signInWithGoogleState: MutableStateFlow<Resource<BeginSignInResult>> =
        MutableStateFlow(Resource.Nothing())
    val signWithGoogleState = _signInWithGoogleState.asStateFlow()

    fun saveOnBoardingState(completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            onBoardingUseCase.saveOnBoardingUseCase(completed = completed)
        }
    }

    fun signUpWithGoogle(context: Context) {
        _signInWithGoogleState.value = Resource.Loading()
        val oneTapClient = Identity.getSignInClient(context)
        val signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(Constants.GOOGLE_WEB_CLIENT)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { task ->

            try {
                _signInWithGoogleState.value = Resource.Success(task)
            } catch (e: IntentSender.SendIntentException) {

                _signInWithGoogleState.value =
                    Resource.Error(e.localizedMessage)

            }

        }.addOnFailureListener {
            _signInWithGoogleState.value = Resource.Error(it.localizedMessage)
        }
    }

}