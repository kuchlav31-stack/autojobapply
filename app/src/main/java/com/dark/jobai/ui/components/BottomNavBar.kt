package com.dark.jobai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.ui.theme.*

/**
 * 5-tab bottom navigation bar
 */
@Composable
fun BottomNavBar5(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = Color(0xFF0D0D0D),
        contentColor = TextWhite,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Jobs
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    Icons.Default.Work,
                    contentDescription = "Jobs"
                )
            },
            label = {
                Text(
                    "Jobs",
                    fontSize = 9.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors(selectedTab == 0)
        )

        // Tab 2: Feed
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    Icons.Default.DynamicFeed,
                    contentDescription = "Feed"
                )
            },
            label = {
                Text(
                    "Feed",
                    fontSize = 9.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors(selectedTab == 1)
        )

        // Tab 3: Applied
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "Applied"
                )
            },
            label = {
                Text(
                    "Applied",
                    fontSize = 9.sp,
                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors(selectedTab == 2)
        )

        // Tab 4: Pricing
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Pricing"
                )
            },
            label = {
                Text(
                    "Pricing",
                    fontSize = 9.sp,
                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors(selectedTab == 3)
        )

        // Tab 5: Profile
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text(
                    "Profile",
                    fontSize = 9.sp,
                    fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors(selectedTab == 4)
        )
    }
}

@Composable
private fun navItemColors(isSelected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryGreen,
    selectedTextColor = PrimaryGreen,
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray,
    indicatorColor = SurfaceDark
)