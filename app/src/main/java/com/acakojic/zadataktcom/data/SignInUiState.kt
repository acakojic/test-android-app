package com.acakojic.zadataktcom.data

data class SignInUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val token: String? = null,
    val errorMessage: String = "",
    val isLoggedIn: Boolean = false
)
