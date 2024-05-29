package com.acakojic.zadataktcom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.acakojic.zadataktcom.factory.SignInViewModelFactory
import com.acakojic.zadataktcom.viewmodel.SignInViewModel
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.ui.theme.ZadatakTcomTheme

//for later
//todo needs to add strings to strings.xml
//todo network catch errors
//todo refactor code
//todo add more logs
class SignInActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: Context = this

        val authRepository =
            CustomRepository(context)
        val signInViewModel = ViewModelProvider(
            this,
            SignInViewModelFactory(authRepository, context)
        ).get(SignInViewModel::class.java)


        setContent {
            ZadatakTcomTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignInScreen(signInViewModel = signInViewModel) {
                        navigateToNextScreen()
                    }
                }
            }
        }
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun SignInScreen(signInViewModel: SignInViewModel, navigateToNextScreen: () -> Unit) {
    val context = LocalContext.current
    val uiState by signInViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("a.kojic@yahoo.com") }

    if (uiState.isSuccess) {
        Log.d("SignInActivity", "SignInScreen: ")
        // TODO: Navigate to the next page
        navigateToNextScreen()
    }

    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_icon),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Title
            Text("Tcom Test Login", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isLoggedIn) {

                // Email input field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    //todo use string.xml
                    label = { Text("Unesite email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.background(Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Sign In Button
                Button(
                    onClick = {
                        signInViewModel.signIn(email)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Prijavi se", color = Color.White)
                    }
                }

                if (uiState.isError) {
                    Text(uiState.errorMessage, color = Color.Red)
                }
            }
            else {
                // Logged in
                // start new Activity
                Button(
                    onClick = {
                        navigateToNextScreen()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Udji u aplikaciju", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // show logout button
                Button(
                    onClick = {
                        signInViewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}