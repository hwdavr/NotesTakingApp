package com.example.notesapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(onBack: () -> Unit, onSignup: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            AuthHeader(title = "Sign Up", onBack = onBack)
        },
        containerColor = Color(0xFFECECF2)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", placeholder = "John Doe", cornerRadius = 14)
                AuthTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "your@email.com", cornerRadius = 14)
                AuthTextField(value = password, onValueChange = { password = it; passwordError = null }, label = "Password", placeholder = "••••••••", isPassword = true, isError = passwordError != null, cornerRadius = 14)
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
                AuthTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", placeholder = "••••••••", isPassword = true, cornerRadius = 14)
            }

            AuthBottomSection(
                buttonText = "Sign Up",
                onButtonClick = {
                    val state = validatePassword(password)
                    if (!state.isOverallValid) {
                        passwordError = "Password must contain at least 8 characters, and at least 3 of the following: lower case letters, upper case letters, numbers, special characters."
                    } else {
                        passwordError = null
                        onSignup()
                    }
                },
                termsText = "By continuing, you agree to our Terms of Service and Privacy Policy."
            )
        }
    }
}
