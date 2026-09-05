package com.dark.jobai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.Job
import com.dark.jobai.util.Formatters

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    onEmailApply: () -> Unit = {},
    onSave: () -> Unit = {},
    isSaved: Boolean = false,
    isApplied: Boolean = false // New: Shows Applied ✓ if already applied
) {
    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val SuccessGreen = Color(0xFF10B981)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ============ HEADER ROW (Logo, Company, Title, Bookmark/Save) ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company Logo Container
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (job.companyLogo.isNotEmpty()) {
                            AsyncImage(
                                model = job.companyLogo,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else {
                            Text(
                                text = job.company.take(1).uppercase(),
                                color = AppBlue,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Company Name & Job Title
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = job.company,
                        color = AppBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = job.title,
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Bookmark / Save Job Button (Replaced Heart with Bookmark)
                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSaved) AppBlue.copy(alpha = 0.1f) else Color(0xFFF8FAFC))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Job",
                        tint = if (isSaved) AppBlue else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Match Score Badge
            if (job.matchScore > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                MatchScoreBadge(score = job.matchScore)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Short Description
            Text(
                text = job.description.ifEmpty { "No description provided for this position." },
                color = TextMuted,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tags / Info Chips (Location, Work Type, Salary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernChip(Icons.Default.LocationOn, job.location)
                ModernChip(Icons.Default.WorkOutline, job.workType)
                if (job.salary != "Not Disclosed") {
                    ModernChip(Icons.Default.Payments, job.salary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ============ ACTION BUTTONS (With "Applied ✓" Status) ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // If Already Applied -> Show Disabled Green "Applied ✓" Button
                if (isApplied) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Applied ✓",
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (job.hasEmail) {
                    // Not applied yet -> Show Active "Auto Apply"
                    OutlinedButton(
                        onClick = onEmailApply,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, AppBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBlue)
                    ) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto Apply",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // View Details Button
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Posted Time Footer
            Text(
                text = "Posted ${Formatters.formatTimeAgo(job.postedAt)}",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ModernChip(icon: ImageVector, text: String) {
    val AppBlue = Color(0xFF0F52FF)
    val TextMuted = Color(0xFF64748B)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppBlue,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MatchScoreBadge(score: Int) {
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val InfoBlue = Color(0xFF3B82F6)

    val color = when {
        score >= 80 -> SuccessGreen
        score >= 60 -> WarningOrange
        else -> InfoBlue
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$score% Match for your profile",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}