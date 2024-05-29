package com.acakojic.zadataktcom.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acakojic.zadataktcom.data.SignInUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.utility.EncryptedSharedPredManager

class SignInViewModel(private val customRepository: CustomRepository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState

    init {
        checkIfUserIsLoggedIn()
    }

    fun signIn(email: String) {
        viewModelScope.launch {
            _uiState.value = SignInUiState(isLoading = true)
            try {
                val response = customRepository.login(email)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("SignInViewModel", "signIn success!")

                    val loginResponse = response.body()!!
                    Log.d("SignInViewModel", "signIn success response: $loginResponse")
                    EncryptedSharedPredManager.saveCredentials(
                        context = context, token = loginResponse.token, email = loginResponse.user.email)

                    _uiState.value = SignInUiState(
                        isSuccess = true,
                        token = response.body()?.token,
                        isLoggedIn = true)
                    // TODO: Navigate to the next page

                } else {
                    Log.w("SignInViewModel", "signIn failed!")
                    _uiState.value = SignInUiState(errorMessage = "Login failed")
                }
            } catch (e: Exception) {
                Log.e("SignInViewModel", "signIn failed error!")
                _uiState.value = SignInUiState(errorMessage = e.localizedMessage ?: "An error occurred")
            }
        }
    }

    private fun checkIfUserIsLoggedIn() {
        val token = EncryptedSharedPredManager.getToken(context = context)
        _uiState.value = _uiState.value.copy(isLoggedIn = !token.isNullOrBlank())
    }

    fun logout() {
        EncryptedSharedPredManager.clearCredentials(context = context)
        _uiState.value = SignInUiState(isLoggedIn = false)
    }
}