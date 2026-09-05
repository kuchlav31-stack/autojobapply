package com.dark.jobai.ui.screens.main.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Work type filter chips with professional light styling
 */
@Composable
fun WorkTypeFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("All", "Remote", "Hybrid", "On-site").forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        filter,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isSelected) AppBlue else SurfaceWhite,
                    labelColor = if (isSelected) Color.White else TextMuted,
                    selectedContainerColor = AppBlue,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderSubtle,
                    selectedBorderColor = AppBlue
                ),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

/**
 * Email only toggle with professional styling
 */
@Composable
fun EmailOnlyToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Show jobs with direct email",
            color = TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BorderSubtle,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}