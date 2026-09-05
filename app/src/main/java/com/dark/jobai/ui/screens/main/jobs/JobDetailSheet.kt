package com.dark.jobai.ui.screens.main.jobs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.autojobapply.InfoTag
import com.dark.jobai.data.model.Job
import com.dark.jobai.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailSheet(
    job: Job,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onEmailApply: () -> Unit = {},
    isEmailSending: Boolean = false
) {
    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        contentColor = TextDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Company Info Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (job.companyLogo.isNotEmpty()) {
                    AsyncImage(
                        model = job.companyLogo,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            job.company.take(1).uppercase(),
                            color = AppBlue,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        job.company,
                        color = AppBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        job.title,
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Info Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTag(Icons.Default.LocationOn, job.location)
                InfoTag(Icons.Default.WorkOutline, job.workType)
                InfoTag(Icons.Default.Payments, Formatters.formatSalary(job.salary))
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(color = BorderSubtle)

            Spacer(modifier = Modifier.height(18.dp))

            // Description Section
            Text(
                "Job Description",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                job.description.ifEmpty { "No description available" },
                color = TextDark.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            // Contact Email Section
            if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Contact Email",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    job.contactEmail,
                    color = AppBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Skills / Tags Section
            if (job.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Required Skills",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    job.tags.take(5).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppBlue.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                tag,
                                color = AppBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email Apply Button
                if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onEmailApply,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        border = BorderStroke(1.5.dp, AppBlue),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                    ) {
                        if (isEmailSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AppBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Email, null, tint = AppBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email", color = AppBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Apply Now Button
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Apply Now", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Posted Date Footer
            Text(
                "Posted: ${Formatters.formatTimeAgo(job.postedAt)}",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}