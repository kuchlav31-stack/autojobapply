package com.dark.jobai.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dark.jobai.ui.components.BottomNavBar5
import com.dark.jobai.ui.screens.main.applications.ApplicationsScreen
import com.dark.jobai.ui.screens.main.feed.FeedScreen
import com.dark.jobai.ui.screens.main.jobs.JobsScreen
import com.dark.jobai.ui.screens.main.pricing.PricingScreen
import com.dark.jobai.ui.screens.main.profile.ProfileScreen
import com.dark.jobai.ui.theme.BackgroundDark

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
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            BottomNavBar5(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> JobsScreen(
                    onJobClick = { jobId -> onJobDetailClick(jobId) },
                    onEmailTemplateNeeded = onEmailTemplateNeeded,
                    onProfileNeeded = onProfileNeeded
                )
                1 -> FeedScreen()
                2 -> ApplicationsScreen()
                3 -> PricingScreen(
                    onUpgradeClick = onUpgradeClick
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