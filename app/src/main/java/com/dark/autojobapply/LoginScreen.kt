package com.dark.autojobapply

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.autojobapply.ui.theme.BackgroundDark
import com.dark.autojobapply.ui.theme.BorderGray
import com.dark.autojobapply.ui.theme.ErrorRed
import com.dark.autojobapply.ui.theme.PrimaryGreen
import com.dark.autojobapply.ui.theme.TextGray
import com.dark.autojobapply.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    googleSignInLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    var viewAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        viewAlpha = 1f
    }

    val entranceAlpha by animateFloatAsState(
        targetValue = viewAlpha,
        animationSpec = tween(durationMillis = 800), label = "AlphaEntrance"
    )

    fun ensureUserDocument(uid: String, name: String, emailAddr: String, provider: String) {
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    val userPayload = hashMapOf(
                        "uid" to uid,
                        "fullName" to name,
                        "email" to emailAddr,
                        "provider" to provider,
                        "createdAt" to System.currentTimeMillis(),
                        "skills" to emptyList<String>(),
                        "preferredRoles" to emptyList<String>(),
                        "experienceYears" to "",
                        "workPreference" to "Remote"
                    )
                    userRef.set(userPayload)
                        .addOnSuccessListener {
                            isLoading = false
                            onSignInSuccess()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            onShowMessage("Database sync failed: ${e.localizedMessage}")
                        }
                } else {
                    isLoading = false
                    onSignInSuccess()
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                onShowMessage("Database check failed: ${e.localizedMessage}")
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .alpha(entranceAlpha)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AutoJobApply",
                color = PrimaryGreen,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Sign in to continue automating your applications.",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 6.dp, bottom = 40.dp),
                textAlign = TextAlign.Center
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Email Address", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; validationError = null },
                    placeholder = { Text("name@domain.com", color = TextGray.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = PrimaryGreen
                    ),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Password", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; validationError = null },
                    placeholder = { Text("••••••••", color = TextGray.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = PrimaryGreen
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
                    trailingIcon = {
                        val iconRes = if (passwordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_secure
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    singleLine = true
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = validationError!!, color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading || googleSignInLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(28.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val trimmedEmail = email.trim()

                            if (trimmedEmail.isEmpty() || password.isEmpty()) {
                                validationError = "Please complete all entry parameters."
                                return@Button
                            }
                            if (!trimmedEmail.matches(emailRegex)) {
                                validationError = "Please write a correct email syntax."
                                return@Button
                            }

                            isLoading = true
                            auth.signInWithEmailAndPassword(trimmedEmail, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        isLoading = false
                                        onSignInSuccess()
                                    } else {
                                        isLoading = false
                                        val err = task.exception?.localizedMessage ?: "Sign-in error."
                                        onShowMessage(err)
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
                Text(
                    text = "OR SIGN IN WITH",
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp),
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (!googleSignInLoading) onGoogleSignInClick() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !googleSignInLoading && !isLoading
                ) {
                    if (googleSignInLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryGreen, strokeWidth = 2.dp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google Sign In",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (activity == null || isLoading || googleSignInLoading) {
                            if (activity == null) onShowMessage("Activity context missing.")
                            return@OutlinedButton
                        }

                        isLoading = true
                        val providerBuilder = OAuthProvider.newBuilder("github.com")
                        providerBuilder.scopes = listOf("user:email")

                        auth.startActivityForSignInWithProvider(activity, providerBuilder.build())
                            .addOnSuccessListener { authResult ->
                                val firebaseUser = authResult.user
                                if (firebaseUser != null) {
                                    ensureUserDocument(
                                        uid = firebaseUser.uid,
                                        name = firebaseUser.displayName ?: "GitHub User",
                                        emailAddr = firebaseUser.email ?: "",
                                        provider = "GitHub OAuth"
                                    )
                                } else {
                                    isLoading = false
                                    onSignInSuccess()
                                }
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                val errMsg = exception.localizedMessage ?: "GitHub OAuth failed."
                                onShowMessage(errMsg)
                            }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !googleSignInLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = "GitHub Sign In",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "GitHub", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Don't have an account? ", color = TextGray, fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = PrimaryGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToSignUp
                    )
                )
            }
        }
    }
}