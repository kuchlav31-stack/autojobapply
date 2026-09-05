package com.dark.jobai.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dark.autojobapply.R
import com.dark.jobai.viewmodel.AuthState
import com.dark.jobai.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Handle AuthState transitions automatically (Logic preserved)
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.MainScreen, is AuthState.ProfileSetup -> {
                onLoginSuccess()
            }
            else -> {}
        }
    }

    // --- Google Sign-In Setup ---
    val webClientId = stringResource(R.string.default_web_client_id)
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    authViewModel.signInWithGoogle(idToken)
                } else {
                    Toast.makeText(context, "Google ID Token is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)       // Vibrant, trustworthy Royal Blue
    val BgLight = Color(0xFFF8FAFC)       // Clean enterprise light grey/blue
    val SurfaceWhite = Color(0xFFFFFFFF)  // Pure white card background
    val TextDark = Color(0xFF0F172A)      // Deep slate for readability
    val TextMuted = Color(0xFF64748B)     // Professional muted slate
    val BorderSubtle = Color(0xFFE2E8F0)  // Ultra clean border color

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // --- APP BRANDING LOGO ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            6.dp,
                            RoundedCornerShape(12.dp),
                            ambientColor = Color.White.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.skills),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "JobAI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- HEADER TEXT ---
            Text(
                text = "Welcome back",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Log in to continue your automated job hunt journey.",
                fontSize = 14.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- ERROR BANNER ---
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // --- MAIN CARD CONTAINER ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.05f)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfessionalLoginTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        icon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfessionalLoginTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Password,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    // --- FORGOT PASSWORD ---
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onForgotPasswordClick() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- LOGIN BUTTON ---
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            authViewModel.signInWithEmail(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Log In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // --- DIVIDER ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
                        Text(
                            text = "OR CONTINUE WITH",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            letterSpacing = 0.5.sp
                        )
                        Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- GOOGLE SIGN-IN BUTTON ---
                    OutlinedButton(
                        onClick = {
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                googleLauncher.launch(signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- FOOTER REDIRECT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    color = AppBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToSignUp() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Reusable Clean Professional TextField Component for Login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0F52FF),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
            cursorColor = Color(0xFF0F52FF)
        )
    )
}