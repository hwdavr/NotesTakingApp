package com.example.notesapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            AuthHeader(title = "Log in", onBack = onBack)
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AuthTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "your@email.com")
                AuthTextField(value = password, onValueChange = { password = it }, label = "Password", placeholder = "••••••••", isPassword = true)
                
                Text(
                    text = "Forgot password?",
                    color = Color(0xFF6D63F5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            AuthBottomSection(
                buttonText = "Log in",
                onButtonClick = onLogin,
                termsText = "By continuing, you agree to our Terms of Service and Privacy Policy."
            )
        }
    }
}
