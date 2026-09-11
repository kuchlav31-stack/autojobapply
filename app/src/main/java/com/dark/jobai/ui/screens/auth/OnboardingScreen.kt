package com.dark.jobai.ui.screens.auth

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.dark.autojobapply.R
// ─────────────────────────────────────────────
// Onboarding Model
// ─────────────────────────────────────────────

data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val eyebrow: String
)

// ─────────────────────────────────────────────
// Main Onboarding Screen
// ─────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {

    // ─────────────────────────────────────────
    // Pages
    // ─────────────────────────────────────────

    val pages = listOf(

        OnboardingPage(
            eyebrow = "SMARTER JOB SEARCH",
            title = "Find the Right Jobs",
            description = "Discover relevant opportunities without spending hours searching through job boards.",
            imageRes = R.drawable.onboarding_jobs
        ),

        OnboardingPage(
            eyebrow = "AUTOMATED APPLICATIONS",
            title = "Auto Apply for You",
            description = "Let ApplyAI handle repetitive applications while you focus on preparing for your next opportunity.",
            imageRes = R.drawable.onboarding_apply
        ),

        OnboardingPage(
            eyebrow = "STAY INFORMED",
            title = "Get Daily Updates",
            description = "Receive daily email reports and keep track of your applications, responses, and new matches.",
            imageRes = R.drawable.onboarding_updates
        ),

        OnboardingPage(
            eyebrow = "YOUR CAREER, SIMPLIFIED",
            title = "Your Career, On Autopilot",
            description = "Less effort. More opportunities. A smarter way to move your career forward.",
            imageRes = R.drawable.onboarding_autopilot
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    val scope = rememberCoroutineScope()

    // ─────────────────────────────────────────
    // Premium Light Palette
    // ─────────────────────────────────────────

    val BackgroundTop = Color(0xFFF9FCFF)
    val BackgroundBottom = Color(0xFFEAF3FF)

    val PrimaryBlue = Color(0xFF1769FF)
    val PrimaryBlueDark = Color(0xFF1256D8)

    val TextDark = Color(0xFF102A5C)
    val TextMuted = Color(0xFF7183A3)

    val IndicatorInactive = Color(0xFFD5E2F5)

    // ─────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────

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

        // Soft decorative background circle
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-70).dp)
                .size(240.dp)
                .background(
                    color = Color(0xFFE8F1FF),
                    shape = RoundedCornerShape(120.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─────────────────────────────────
            // Top Bar
            // ─────────────────────────────────

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onFinishOnboarding,
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                ) {
                    Text(
                        text = "Skip",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ─────────────────────────────────
            // Pager
            // ─────────────────────────────────

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                pageSpacing = 0.dp
            ) { pageIndex ->

                val page = pages[pageIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(12.dp))

                    // ─────────────────────────
                    // Small Eyebrow
                    // ─────────────────────────

                    Text(
                        text = page.eyebrow,
                        color = PrimaryBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // ─────────────────────────
                    // Main Illustration
                    // ─────────────────────────

                    AnimatedContent(
                        targetState = page.imageRes,
                        transitionSpec = {
                            (
                                    fadeIn(
                                        animationSpec = tween(
                                            450,
                                            easing = FastOutSlowInEasing
                                        )
                                    ) +
                                            scaleIn(
                                                initialScale = 0.94f,
                                                animationSpec = tween(
                                                    450,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                    ).togetherWith(
                                    fadeOut(
                                        animationSpec = tween(250)
                                    ) +
                                            scaleOut(
                                                targetScale = 1.04f,
                                                animationSpec = tween(250)
                                            )
                                )
                        },
                        label = "onboardingIllustration"
                    ) { imageRes ->

                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = page.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(horizontal = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─────────────────────────
                    // Title
                    // ─────────────────────────

                    AnimatedContent(
                        targetState = page.title,
                        transitionSpec = {
                            fadeIn(tween(350)) togetherWith fadeOut(tween(200))
                        },
                        label = "onboardingTitle"
                    ) { title ->

                        Text(
                            text = title,
                            color = TextDark,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-0.7).sp,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ─────────────────────────
                    // Description
                    // ─────────────────────────

                    AnimatedContent(
                        targetState = page.description,
                        transitionSpec = {
                            fadeIn(tween(350)) togetherWith fadeOut(tween(200))
                        },
                        label = "onboardingDescription"
                    ) { description ->

                        Text(
                            text = description,
                            color = TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }

            // ─────────────────────────────────
            // Page Indicators
            // ─────────────────────────────────

            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                repeat(pages.size) { index ->

                    val isSelected = pagerState.currentPage == index

                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(
                                if (isSelected) 24.dp else 7.dp
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected)
                                    PrimaryBlue
                                else
                                    IndicatorInactive
                            )
                    )
                }
            }

            // ─────────────────────────────────
            // Bottom Button
            // ─────────────────────────────────

            Button(
                onClick = {

                    if (pagerState.currentPage < pages.lastIndex) {

                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }

                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {

                Text(
                    text = if (
                        pagerState.currentPage == pages.lastIndex
                    ) {
                        "Get Started  →"
                    } else {
                        "Continue  →"
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}