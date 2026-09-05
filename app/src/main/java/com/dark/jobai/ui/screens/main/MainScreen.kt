package com.dark.jobai.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dark.jobai.ui.components.BottomNavBar5
import com.dark.jobai.ui.screens.main.applications.ApplicationsScreen
import com.dark.jobai.ui.screens.main.feed.FeedScreen
import com.dark.jobai.ui.screens.main.jobs.JobsScreen
import com.dark.jobai.ui.screens.main.pricing.PricingScreen
import com.dark.jobai.ui.screens.main.profile.ProfileScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onProfileNeeded: () -> Unit,
    onEmailTemplateNeeded: () -> Unit,
    onUpgradeClick: () -> Unit,
    onJobDetailClick: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Optimized State with mutableIntStateOf
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- Professional Light Theme Palette ---
    val BgLight = Color(0xFFF8FAFC)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        bottomBar = {
            BottomNavBar5(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex: Int -> selectedTab = tabIndex }
            )
        }
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgLight)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> JobsScreen(
                    onJobClick = { jobId: String -> onJobDetailClick(jobId) },
                    onEmailTemplateNeeded = onEmailTemplateNeeded,
                    onProfileNeeded = onProfileNeeded
                )
                1 -> FeedScreen()
                2 -> ApplicationsScreen()
                3 -> PricingScreen(
//                    onUpgradeClick = onUpgradeClick
                )
                4 -> ProfileScreen(
                    onLogout = onLogout,
                    onEditProfileClick = onEditProfileClick,
                    onSettingsClick = onSettingsClick,
                    onUpgradeClick = onUpgradeClick,
                    onEmailTemplateClick = onEmailTemplateNeeded
                )
            }
        }
    }
}