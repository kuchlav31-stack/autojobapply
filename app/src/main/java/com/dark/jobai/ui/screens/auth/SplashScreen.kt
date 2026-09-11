package com.dark.autojobapply

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // ─────────────────────────────────────────────
    // Light Theme — BlankLearn reference style
    // ─────────────────────────────────────────────
    val BackgroundTop = Color(0xFFF9FCFF)
    val BackgroundBottom = Color(0xFFEAF3FF)

    val PrimaryBlue = Color(0xFF1769FF)
    val SoftBlue = Color(0xFFE8F0FF)

    val TextDark = Color(0xFF102A5C)
    val TextMuted = Color(0xFF7183A3)
    val BorderColor = Color(0xFFDCE7F7)

    // ─────────────────────────────────────────────
    // Animation States
    // ─────────────────────────────────────────────
    val logoScale = remember { Animatable(0.72f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val bottomAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(
        label = "splashAnimation"
    )

    // Very subtle breathing animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(key1 = true) {

        // ─────────────────────────────────────────
        // Entrance Animations
        // ─────────────────────────────────────────
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }

        delay(220)

        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 550,
                    easing = FastOutSlowInEasing
                )
            )
        }

        delay(250)

        launch {
            bottomAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 450,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // ─────────────────────────────────────────
        // Display Duration
        // ─────────────────────────────────────────
        delay(1400)

        // ─────────────────────────────────────────
        // Intelligent Routing Logic
        // UNCHANGED
        // ─────────────────────────────────────────
        try {
            val prefs = context.getSharedPreferences(
                "jobai_prefs",
                Context.MODE_PRIVATE
            )

            val hasSeenOnboarding = prefs.getBoolean(
                "has_seen_onboarding",
                false
            )

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {

                // Logged in -> Check profile setup status
                val userId = currentUser.uid

                val firestoreDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val isProfileCompleted =
                    firestoreDoc.getBoolean("isProfileCompleted") ?: true

                if (isProfileCompleted) {
                    onNavigateNext(AppRoutes.MAIN)
                } else {
                    onNavigateNext(AppRoutes.PROFILE_SETUP)
                }

            } else if (!hasSeenOnboarding) {

                // First time user -> Show Onboarding Explainer
                prefs.edit()
                    .putBoolean("has_seen_onboarding", true)
                    .apply()

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

    // ─────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundTop,
                        BackgroundTop,
                        BackgroundBottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ─────────────────────────────────────────
        // Soft Decorative Background Shapes
        // ─────────────────────────────────────────

        // Top-right soft blue circle
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-60).dp)
                .size(220.dp)
                .background(
                    color = Color(0xFFE8F1FF),
                    shape = RoundedCornerShape(110.dp)
                )
        )

        // Bottom-left soft blue circle
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .size(250.dp)
                .background(
                    color = Color(0xFFE2EEFF),
                    shape = RoundedCornerShape(125.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─────────────────────────────────────
            // Top Brand Label
            // ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(50)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "SMARTER JOB SEARCH",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // ─────────────────────────────────────
            // Center Content
            // ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ─────────────────────────────────
                // Logo Card
                // ─────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .scale(logoScale.value * pulseScale)
                        .alpha(logoAlpha.value)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(36.dp),
                            ambientColor = PrimaryBlue.copy(alpha = 0.12f),
                            spotColor = PrimaryBlue.copy(alpha = 0.18f)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    // Soft inner blue surface
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .background(
                                color = SoftBlue,
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Image(
                            painter = painterResource(
                                id = R.drawable.logo
                            ),
                            contentDescription = "ApplyAI Logo",
                            modifier = Modifier.size(104.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                // ─────────────────────────────────
                // Brand Name
                // ─────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(textAlpha.value)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Apply",
                            color = TextDark,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1.2).sp
                        )

                        Text(
                            text = "AI",
                            color = PrimaryBlue,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1.2).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your career, on autopilot.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp
                    )
                }
            }

            // ─────────────────────────────────────
            // Bottom Loading Section
            // ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .alpha(bottomAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Preparing your experience...",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Track
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(3.dp)
                        .background(
                            color = BorderColor,
                            shape = RoundedCornerShape(50)
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .fillMaxHeight()
                            .background(
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "ApplyAI",
                    color = Color(0xFF9AAAC4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}