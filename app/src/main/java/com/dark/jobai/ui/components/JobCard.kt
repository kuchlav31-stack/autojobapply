package com.dark.jobai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.Job
import com.dark.jobai.ui.theme.*
import com.dark.jobai.util.Formatters

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    onEmailApply: () -> Unit = {},
    onSave: () -> Unit = {},
    isSaved: Boolean = false
) {
    var isLiked by remember { mutableStateOf(isSaved) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (job.matchScore >= 80) SuccessGreen.copy(alpha = 0.3f) else Color.Transparent
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            width = 1.5.dp,
            color = when {
                job.matchScore >= 80 -> SuccessGreen.copy(alpha = 0.5f)
                job.matchScore >= 60 -> WarningOrange.copy(alpha = 0.4f)
                job.hasEmail -> PrimaryGreen.copy(alpha = 0.3f)
                else -> BorderGray.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ============ TOP ROW: Logo + Title + Save ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Company Logo with gradient border
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SurfaceElevated, SurfaceDark)
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = PrimaryGreen.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (job.companyLogo.isNotEmpty()) {
                            AsyncImage(
                                model = job.companyLogo,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                job.company.take(1).uppercase(),
                                color = PrimaryGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Match Score Badge (top)
                        if (job.matchScore > 0) {
                            MatchScoreBadge(score = job.matchScore)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            job.title,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                        Text(
                            job.company,
                            color = PrimaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Save Button
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        onSave()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isLiked) ErrorRed else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ============ INFO TAGS ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.LocationOn,
                    text = job.location,
                    color = InfoBlue
                )

                InfoChip(
                    icon = Icons.Default.Business,
                    text = job.workType,
                    color = if (job.workType == "Remote") SuccessGreen else WarningOrange
                )

                if (job.salary != "Not Disclosed") {
                    InfoChip(
                        icon = Icons.Default.Payments,
                        text = job.salary,
                        color = GoldPremium
                    )
                }
            }

            // ============ TAGS ============
            if (job.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    job.tags.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                tag,
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ============ EMAIL INDICATOR ============
            if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Direct email available",
                        color = PrimaryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ============ DIVIDER ============
            HorizontalDivider(color = BorderGray.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(10.dp))

            // ============ BOTTOM ROW: Date + Buttons ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        Formatters.formatTimeAgo(job.postedAt),
                        color = TextGray.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Email Button
                    if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onEmailApply,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryGreen
                            ),
                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(17.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Email",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }

                    // Apply Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (job.matchScore >= 80) SuccessGreen else PrimaryGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Apply",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// MATCH SCORE BADGE
// ====================================================================

@Composable
fun MatchScoreBadge(score: Int) {
    val (color, label) = when {
        score >= 80 -> SuccessGreen to "Excellent Match"
        score >= 60 -> WarningOrange to "Good Match"
        score >= 40 -> InfoBlue to "Fair Match"
        else -> TextGray to "Low Match"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Bolt,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            "$score% Match",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ====================================================================
// INFO CHIP
// ====================================================================

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    color: Color = TextGray
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            color = TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}