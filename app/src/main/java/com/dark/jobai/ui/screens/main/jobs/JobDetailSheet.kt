package com.dark.jobai.ui.screens.main.jobs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.autojobapply.InfoTag
import com.dark.jobai.data.model.Job
import com.dark.jobai.ui.theme.*
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Company Info
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
                                    colors = listOf(PrimaryGreen, Color(0xFF00D2A0))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            job.company.take(1).uppercase(),
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        job.company,
                        color = PrimaryGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        job.title,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTag(Icons.Default.LocationOn, job.location)
                InfoTag(Icons.Default.Work, job.workType)
                InfoTag(Icons.Default.Payments, Formatters.formatSalary(job.salary))
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                "Job Description",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                job.description.ifEmpty { "No description available" },
                color = TextWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Contact Email
            if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Contact Email",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    job.contactEmail,
                    color = PrimaryGreen,
                    fontSize = 14.sp
                )
            }

            // Tags
            if (job.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Skills",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    job.tags.take(5).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                tag,
                                color = PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email Apply
                if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onEmailApply,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isEmailSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Email, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email", color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Apply Now
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Now", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Posted date
            Text(
                "Posted: ${Formatters.formatTimeAgo(job.postedAt)}",
                color = TextGray.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}