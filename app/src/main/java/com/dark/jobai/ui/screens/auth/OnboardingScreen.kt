package com.dark.jobai.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlight: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Automated AI Job Hunt",
            description = "Stop filling forms manually. Our AI agent discovers and applies to top tech & professional jobs for you 24/7.",
            icon = Icons.Default.SmartToy,
            highlight = "100% Automated"
        ),
        OnboardingPage(
            title = "Direct Recruiter Outreach",
            description = "We find direct HR and hiring manager emails so your resume lands right in their inbox, bypassing job board noise.",
            icon = Icons.Default.MarkEmailRead,
            highlight = "Direct Email Delivery"
        ),
        OnboardingPage(
            title = "Land Interviews 5x Faster",
            description = "Track delivery status, recruiter opens, and view counts in real-time. Supercharge your career growth today.",
            icon = Icons.Default.TrendingUp,
            highlight = "Real-Time Tracking"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinishOnboarding) {
                    Text("Skip", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal Pager for Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Highlight Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppBlue.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = page.highlight,
                            color = AppBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Large Feature Icon Box
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = SurfaceWhite,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                tint = AppBlue,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = page.description,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Pager Indicators (Dots)
            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) AppBlue else BorderSubtle)
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            // Bottom Action Button (Next / Get Started)
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started " else "Next",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}