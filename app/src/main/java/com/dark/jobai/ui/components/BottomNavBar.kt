package com.dark.jobai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 5-tab professional bottom navigation bar (Light Theme Optimized)
 */
@Composable
fun BottomNavBar5(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val tabs = listOf(
                Triple(Icons.Default.Explore, "Explore", 0),
                Triple(Icons.Default.Public, "Feed", 1),
                Triple(Icons.Default.AssignmentTurnedIn, "History", 2),
                Triple(Icons.Default.Star, "Premium", 3),
                Triple(Icons.Default.Person, "Profile", 4)
            )

            tabs.forEach { (icon, label, index) ->
                val isSelected = selectedTab == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) AppBlue else TextMuted,
                                modifier = Modifier.size(
                                    if (isSelected) 24.dp else 22.dp
                                )
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(AppBlue, CircleShape)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.3.sp,
                            color = if (isSelected) AppBlue else TextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}