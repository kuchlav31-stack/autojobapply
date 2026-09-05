package com.dark.jobai.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.ui.components.AppTextField
import com.dark.jobai.ui.components.PrimaryButton
import com.dark.jobai.ui.theme.*
import com.dark.jobai.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    var email by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(28.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LockReset,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Reset Password",
                color = TextWhite,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )

            Text(
                text = "We'll send a recovery link to your email",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "Enter your registered email",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = if (errorMessage == "Password reset email sent!") SuccessGreen else ErrorRed,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Send Recovery Link",
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                return@PrimaryButton
                            }
                            authViewModel.resetPassword(email.trim())
                        },
                        isLoading = isLoading,
                        icon = Icons.Default.Send
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onBack) {
                Text(
                    "Back to Sign In",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
