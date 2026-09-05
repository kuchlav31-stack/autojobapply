package com.dark.jobai.ui.screens.main.jobs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.Job
import com.dark.jobai.data.repository.EmailRepository
import com.dark.jobai.data.repository.JobRepository
import com.dark.jobai.util.Formatters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    onBack: () -> Unit,
    onProfileNeeded: () -> Unit = {},
    onEmailTemplateNeeded: () -> Unit = {}
) {
    val context = LocalContext.current
    val jobRepository = remember { JobRepository() }
    val scope = rememberCoroutineScope()

    var job by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaved by remember { mutableStateOf(false) }
    var isEmailSending by remember { mutableStateOf(false) }

    // Load job logic unchanged
    LaunchedEffect(jobId) {
        job = jobRepository.getJobById(jobId)
        isLoading = false

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && job != null) {
            isSaved = jobRepository.isJobSaved(userId, jobId)
        }
    }

    // Save job logic unchanged
    fun toggleSave() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentJob = job ?: return

        scope.launch {
            if (isSaved) {
                jobRepository.removeSavedJob(userId, currentJob.id)
                isSaved = false
                Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show()
            } else {
                jobRepository.saveJob(userId, currentJob)
                isSaved = true
                Toast.makeText(context, "✅ Job saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Email apply logic unchanged
    fun handleEmailApply() {
        val currentJob = job ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        scope.launch {
            val userDoc = try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
            } catch (e: Exception) { null }

            if (userDoc == null || !userDoc.exists()) {
                onProfileNeeded()
                return@launch
            }

            val hasTemplate = userDoc.getString("emailSubjectTemplate")?.isNotEmpty() == true
            if (!hasTemplate) {
                onEmailTemplateNeeded()
                return@launch
            }

            isEmailSending = true

            val emailRepository = EmailRepository()
            val result = emailRepository.sendApplicationEmail(
                userId = userId,
                toEmail = currentJob.contactEmail,
                jobTitle = currentJob.title,
                companyName = currentJob.company
            )

            result.onSuccess {
                isEmailSending = false
                Toast.makeText(context, "✅ Email sent successfully!", Toast.LENGTH_LONG).show()
            }.onFailure { e ->
                isEmailSending = false
                Toast.makeText(context, "❌ ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Open apply URL logic unchanged
    fun openApplyUrl() {
        val currentJob = job ?: return

        if (currentJob.applyUrl.isNotBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentJob.applyUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Professional Light Theme Palette (Matches Login/SignUp) ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val ErrorRed = Color(0xFFEF4444)
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val InfoBlue = Color(0xFF3B82F6)

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
        ) {
            // ============ TOP BAR ============
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextDark, modifier = Modifier.size(20.dp))
                }

                Text(
                    "Job Details",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Save Button
                IconButton(
                    onClick = { toggleSave() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.05f))
                ) {
                    Icon(
                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) ErrorRed else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppBlue)
                    }
                }
                job == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Job not found", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                else -> {
                    val currentJob = job!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // ============ COMPANY HEADER CARD ============
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f)),
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Company Logo
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentJob.companyLogo.isNotEmpty()) {
                                            AsyncImage(
                                                model = currentJob.companyLogo,
                                                contentDescription = "Logo",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                currentJob.company.take(1).uppercase(),
                                                color = AppBlue,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            currentJob.title,
                                            color = TextDark,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 24.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            currentJob.company,
                                            color = AppBlue,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Match Score Badge
                                if (currentJob.matchScore > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                when {
                                                    currentJob.matchScore >= 80 -> SuccessGreen.copy(alpha = 0.12f)
                                                    currentJob.matchScore >= 60 -> WarningOrange.copy(alpha = 0.12f)
                                                    else -> InfoBlue.copy(alpha = 0.12f)
                                                }
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            null,
                                            tint = when {
                                                currentJob.matchScore >= 80 -> SuccessGreen
                                                currentJob.matchScore >= 60 -> WarningOrange
                                                else -> InfoBlue
                                            },
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "${currentJob.matchScore}% Match with your profile",
                                            color = when {
                                                currentJob.matchScore >= 80 -> SuccessGreen
                                                currentJob.matchScore >= 60 -> WarningOrange
                                                else -> InfoBlue
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ============ QUICK INFO CARD ============
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Quick Information",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                DetailRow(Icons.Default.LocationOn, "Location", currentJob.location, TextDark, TextMuted, AppBlue)
                                DetailRow(Icons.Default.Business, "Work Type", currentJob.workType, TextDark, TextMuted, AppBlue)
                                DetailRow(Icons.Default.Payments, "Salary", Formatters.formatSalary(currentJob.salary), TextDark, TextMuted, AppBlue)
                                DetailRow(Icons.Default.Timeline, "Experience", currentJob.experienceRequired, TextDark, TextMuted, AppBlue)
                                DetailRow(Icons.Default.Schedule, "Posted", Formatters.formatTimeAgo(currentJob.postedAt), TextDark, TextMuted, AppBlue)
                                DetailRow(Icons.Default.Source, "Source", currentJob.source, TextDark, TextMuted, AppBlue)

                                if (currentJob.hasEmail && currentJob.contactEmail.isNotEmpty()) {
                                    DetailRow(Icons.Default.Email, "Contact Email", currentJob.contactEmail, TextDark, TextMuted, AppBlue)
                                }

                                if (currentJob.hasPhone && currentJob.contactPhone.isNotEmpty()) {
                                    DetailRow(Icons.Default.Phone, "Contact Phone", currentJob.contactPhone, TextDark, TextMuted, AppBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ============ DESCRIPTION CARD ============
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Job Description",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (currentJob.description.isNotEmpty()) {
                                    Text(
                                        currentJob.description,
                                        color = TextDark.copy(alpha = 0.85f),
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                } else {
                                    Text(
                                        "No detailed description available. Click Apply to view the full job posting.",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // ============ SKILLS/TAGS CARD ============
                        if (currentJob.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                                shape = RoundedCornerShape(20.dp),
                                color = SurfaceWhite,
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        "Skills Required",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val chunkedTags = currentJob.tags.take(9).chunked(3)

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        chunkedTags.forEach { rowTags ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowTags.forEach { tag ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(AppBlue.copy(alpha = 0.08f))
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            tag,
                                                            color = AppBlue,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ============ ACTION BUTTONS ============
                        if (currentJob.hasEmail && currentJob.contactEmail.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { handleEmailApply() },
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                    Icon(Icons.Default.Email, null, tint = AppBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Email Apply",
                                        color = AppBlue,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = { openApplyUrl() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Apply Now",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ====================================================================
// DETAIL ROW COMPONENT (Upgraded Light Theme Style)
// ====================================================================

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    mutedColor: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                label,
                color = mutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                value,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}