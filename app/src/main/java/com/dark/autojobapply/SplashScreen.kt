package com.dark.autojobapply

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.autojobapply.ui.theme.BackgroundDark
import com.dark.autojobapply.ui.theme.PrimaryGreen
import com.dark.autojobapply.ui.theme.TextGray
import com.dark.autojobapply.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Production-grade Splash Screen for autojobApply.
 * Handles animations and routes the user based on active login sessions.
 */
@Composable
fun SplashScreen(
    onNavigateNext: (String) -> Unit
) {
    // Animation States
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // 1. Trigger Logo scale and alpha animations concurrently
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // 2. Delayed fade-in for brand text & tagline
        delay(300)
        launch {
            textAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // 3. Keep displaying Splash to establish brand memory
        delay(1500)

        // 4. Verification Check: Decides if user is authenticated
        try {
            val authInstance = FirebaseAuth.getInstance()
            val currentUser = authInstance.currentUser

            if (currentUser != null) {
                // Verified session -> Direct to Home Dashboard
                onNavigateNext(AppRoutes.HOME)
            } else {
                // No session -> Direct to Authentication
                onNavigateNext(AppRoutes.LOGIN)
            }
        } catch (e: Exception) {
            // Safe fallback: Logcat reporting and direct navigation to prevent frozen frames
            e.printStackTrace()
            onNavigateNext(AppRoutes.LOGIN)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Custom App Logo Icon Wrapper
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .background(
                        color = PrimaryGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "autojobApply Logo Logo",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Typography Layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "autojobApply",
                    color = TextWhite,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your AI Job Application Agent",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}