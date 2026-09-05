package com.dark.autojobapply

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.navigation.AppRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(
    onNavigateNext: (String) -> Unit
) {
    val context = LocalContext.current

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)

    // Animation States
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    // Infinite pulse effect for high-end feel
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(key1 = true) {
        // 1. Entrance animations
        launch {
            logoScale.animateTo(1.0f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            logoAlpha.animateTo(1.0f, tween(600, easing = FastOutSlowInEasing))
        }

        delay(200)
        launch {
            textAlpha.animateTo(1.0f, tween(500, easing = FastOutSlowInEasing))
        }

        // 2. Display duration
        delay(1400)

        // 3. Intelligent Routing Logic
        try {
            val prefs = context.getSharedPreferences("jobai_prefs", Context.MODE_PRIVATE)
            val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // Logged in -> Check profile setup status
                val userId = currentUser.uid
                val firestoreDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val isProfileCompleted = firestoreDoc.getBoolean("isProfileCompleted") ?: true

                if (isProfileCompleted) {
                    onNavigateNext(AppRoutes.MAIN)
                } else {
                    onNavigateNext(AppRoutes.PROFILE_SETUP)
                }
            } else if (!hasSeenOnboarding) {
                // First time user -> Show Onboarding Explainer
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                onNavigateNext(AppRoutes.ONBOARDING)
            } else {
                // Returning user, not logged in -> Login Screen
                onNavigateNext(AppRoutes.LOGIN)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (FirebaseAuth.getInstance().currentUser != null) {
                onNavigateNext(AppRoutes.MAIN)
            } else {
                onNavigateNext(AppRoutes.LOGIN)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgLight, Color(0xFFEEF2F6))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing App Logo
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale.value * pulseScale)
                    .alpha(logoAlpha.value)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color.White.copy(alpha = 0.4f),
                        spotColor = Color.White.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.skills),
                        contentDescription = "JobAI Logo",
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "JobAI",
                    color = TextDark,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Autonomous AI Job Agent",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}