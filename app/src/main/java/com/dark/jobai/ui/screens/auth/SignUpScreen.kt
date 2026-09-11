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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // ═════════════════════════════════════════
    // AUTH LOGIC — UNCHANGED
    // ═════════════════════════════════════════

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.ProfileSetup,
            is AuthState.MainScreen -> {
                onSignUpSuccess()
            }

            else -> {}
        }
    }

    // ═════════════════════════════════════════
    // GOOGLE SIGN-IN — UNCHANGED
    // ═════════════════════════════════════════

    val webClientId = stringResource(R.string.default_web_client_id)

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                val idToken = account?.idToken

                if (idToken != null) {

                    authViewModel.signInWithGoogle(idToken)

                } else {

                    Toast.makeText(
                        context,
                        "Google ID Token is null",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: ApiException) {

                Toast.makeText(
                    context,
                    "Google Sign-In Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ═════════════════════════════════════════
    // DESIGN SYSTEM
    // ═════════════════════════════════════════

    val Blue = Color(0xFF1769FF)
    val Navy = Color(0xFF102A5C)
    val Muted = Color(0xFF7183A3)
    val Border = Color(0xFFDCE7F7)
    val Background = Color(0xFFF7FAFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9FCFF),
                        Background,
                        Color(0xFFEAF3FF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ═════════════════════════════════════
        // SOFT BACKGROUND SHAPES
        // ═════════════════════════════════════

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-90).dp)
                .size(250.dp)
                .background(
                    Color(0xFFE5F0FF),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 100.dp)
                .size(280.dp)
                .background(
                    Color(0xFFE2EEFF),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

//            Spacer(modifier = Modifier.height(18.dp))
//
//            // ═════════════════════════════════
//            // BRAND HEADER
//            // ═════════════════════════════════
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                Image(
//                    painter = painterResource(
//                        id = R.drawable.logo
//                    ),
//                    contentDescription = "ApplyAI Logo",
//                    modifier = Modifier.size(38.dp)
//                )
//
//                Spacer(modifier = Modifier.width(10.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Text(
//                        text = "Apply",
//                        color = Navy,
//                        fontSize = 21.sp,
//                        fontWeight = FontWeight.Bold,
//                        letterSpacing = (-0.7).sp
//                    )
//
//                    Text(
//                        text = "AI",
//                        color = Blue,
//                        fontSize = 21.sp,
//                        fontWeight = FontWeight.Bold,
//                        letterSpacing = (-0.7).sp
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═════════════════════════════════
            // HERO ILLUSTRATION
            // ═════════════════════════════════

            SignUpHeroIllustration(
                blue = Blue,
                navy = Navy
            )

            Spacer(modifier = Modifier.height(21.dp))

            // ═════════════════════════════════
            // HEADER
            // ═════════════════════════════════

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text = "Build your career.\nWe'll handle the search.",
                    color = Navy,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.9).sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Create your account and start your\nautomated job journey.",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(23.dp))

            // ═════════════════════════════════
            // ERROR MESSAGE
            // ═════════════════════════════════

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                errorMessage?.let { msg ->

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 13.dp),
                        color = Color(0xFFFFF3F3),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFFECACA)
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = msg,
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ═════════════════════════════════
            // FULL NAME
            // ═════════════════════════════════

            SignUpInputField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full name",
                icon = Icons.Default.PersonOutline,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ═════════════════════════════════
            // EMAIL
            // ═════════════════════════════════

            SignUpInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                icon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ═════════════════════════════════
            // PASSWORD
            // ═════════════════════════════════

            SignUpInputField(
                value = password,
                onValueChange = { password = it },
                label = "Create password",
                icon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = isPasswordVisible,
                onVisibilityChange = {
                    isPasswordVisible = !isPasswordVisible
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ═════════════════════════════════
            // PASSWORD HINT
            // ═════════════════════════════════

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF18B989),
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Use at least 6 characters",
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(19.dp))

            // ═════════════════════════════════
            // CREATE ACCOUNT
            // ═════════════════════════════════

            Button(
                onClick = {

                    focusManager.clearFocus()

                    if (
                        fullName.isBlank() ||
                        email.isBlank() ||
                        password.isBlank()
                    ) {

                        Toast.makeText(
                            context,
                            "Please fill in all fields",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    if (password.length < 6) {

                        Toast.makeText(
                            context,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    authViewModel.signUpWithEmail(
                        email,
                        password,
                        fullName
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(21.dp),
                        strokeWidth = 2.5.dp
                    )

                } else {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Create account",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(17.dp))

            // ═════════════════════════════════
            // GOOGLE BUTTON
            // ═════════════════════════════════

            OutlinedButton(
                onClick = {

                    googleSignInClient
                        .signOut()
                        .addOnCompleteListener {

                            val signInIntent =
                                googleSignInClient.signInIntent

                            googleLauncher.launch(signInIntent)
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    Border
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.72f)
                )
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.google
                    ),
                    contentDescription = "Google",
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Continue with Google",
                    color = Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(23.dp))

            // ═════════════════════════════════
            // LOGIN FOOTER
            // ═════════════════════════════════

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Already have an account?",
                    color = Muted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Sign in",
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onNavigateToLogin()
                        }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

// ═════════════════════════════════════════════
// SIGNUP HERO ILLUSTRATION
// ═════════════════════════════════════════════

@Composable
private fun SignUpHeroIllustration(
    blue: Color,
    navy: Color
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE8F2FF),
                        Color(0xFFD9EAFF)
                    )
                )
            )
    ) {

        // Background circle

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 35.dp, y = (-35).dp)
                .size(150.dp)
                .background(
                    Color.White.copy(alpha = 0.38f),
                    CircleShape
                )
        )

        // Decorative sparkles

        Text(
            text = "✦",
            color = Color(0xFF8CB7FF),
            fontSize = 25.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 25.dp, y = 24.dp)
        )

        Text(
            text = "✦",
            color = Color(0xFFB2D0FF),
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-25).dp, y = (-20).dp)
        )

        // Main profile/job card

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-8).dp, y = 2.dp)
                .width(232.dp)
                .height(108.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFFEAF2FF)),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = blue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Your career profile",
                            color = navy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Ready for new opportunities",
                            color = Color(0xFF8BA0BE),
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2F8F1)),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF18B989),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(13.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9F0FA))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEAFF))
                    )
                }
            }
        }

        // Floating chip

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-18).dp, y = (-18).dp)
                .rotate(4f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 9.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2F8F1)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF18B989),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Profile created",
                    color = navy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ═════════════════════════════════════════════
// MODERN SIGNUP INPUT
// ═════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
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
        label = {
            Text(
                text = label,
                fontSize = 13.sp
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        visualTransformation = if (
            isPassword && !isPasswordVisible
        ) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,

        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8BA0BE),
                modifier = Modifier.size(19.dp)
            )
        },

        trailingIcon = {
            if (isPassword) {

                IconButton(
                    onClick = onVisibilityChange
                ) {

                    Icon(
                        imageVector = if (isPasswordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = Color(0xFF8BA0BE),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        },

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1769FF),
            unfocusedBorderColor = Color(0xFFDCE7F7),

            focusedContainerColor = Color.White.copy(alpha = 0.72f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.72f),

            focusedTextColor = Color(0xFF102A5C),
            unfocusedTextColor = Color(0xFF102A5C),

            focusedLabelColor = Color(0xFF1769FF),
            unfocusedLabelColor = Color(0xFF8BA0BE),

            cursorColor = Color(0xFF1769FF)
        )
    )
}