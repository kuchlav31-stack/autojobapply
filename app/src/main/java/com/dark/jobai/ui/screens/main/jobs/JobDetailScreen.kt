package com.dark.jobai.ui.screens.main.jobs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.dark.jobai.ui.theme.*
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

    // Load job
    LaunchedEffect(jobId) {
        job = jobRepository.getJobById(jobId)
        isLoading = false

        // Check if saved
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && job != null) {
            isSaved = jobRepository.isJobSaved(userId, jobId)
        }
    }

    // Save job
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
                Toast.makeText(context, "✅ Job saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Email apply
    fun handleEmailApply() {
        val currentJob = job ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        scope.launch {
            // Check profile
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

            // Check email template
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

    // Open apply URL
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // ============ TOP BAR ============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
            }

            Text(
                "Job Details",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Save Button
            IconButton(
                onClick = { toggleSave() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
            ) {
                Icon(
                    if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) ErrorRed else TextGray
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            job == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Job not found", color = TextGray, fontSize = 16.sp)
                }
            }
            else -> {
                val currentJob = job!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // ============ COMPANY HEADER ============
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                                                colors = listOf(SurfaceElevated, SurfaceDark)
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
                                            color = PrimaryGreen,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        currentJob.title,
                                        color = TextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 24.sp
                                    )
                                    Text(
                                        currentJob.company,
                                        color = PrimaryGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Match Score
                            if (currentJob.matchScore > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            when {
                                                currentJob.matchScore >= 80 -> SuccessGreen.copy(alpha = 0.15f)
                                                currentJob.matchScore >= 60 -> WarningOrange.copy(alpha = 0.15f)
                                                else -> InfoBlue.copy(alpha = 0.15f)
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
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

                    // ============ QUICK INFO ============
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Quick Information",
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow(Icons.Default.LocationOn, "Location", currentJob.location)
                            DetailRow(Icons.Default.Business, "Work Type", currentJob.workType)
                            DetailRow(Icons.Default.Payments, "Salary", Formatters.formatSalary(currentJob.salary))
                            DetailRow(Icons.Default.Timeline, "Experience", currentJob.experienceRequired)
                            DetailRow(Icons.Default.Schedule, "Posted", Formatters.formatTimeAgo(currentJob.postedAt))
                            DetailRow(Icons.Default.Source, "Source", currentJob.source)

                            if (currentJob.hasEmail && currentJob.contactEmail.isNotEmpty()) {
                                DetailRow(Icons.Default.Email, "Contact Email", currentJob.contactEmail)
                            }

                            if (currentJob.hasPhone && currentJob.contactPhone.isNotEmpty()) {
                                DetailRow(Icons.Default.Phone, "Contact Phone", currentJob.contactPhone)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ============ DESCRIPTION ============
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Job Description",
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (currentJob.description.isNotEmpty()) {
                                Text(
                                    currentJob.description,
                                    color = TextWhite.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Text(
                                    "No detailed description available. Click Apply to view the full job posting.",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // ============ SKILLS/TAGS ============
                    if (currentJob.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Skills Required",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
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
                                                        .background(PrimaryGreen.copy(alpha = 0.15f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        tag,
                                                        color = PrimaryGreen,
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
                            border = BorderStroke(1.5.dp, PrimaryGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isEmailSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Email, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Email Apply",
                                    color = PrimaryGreen,
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
                            containerColor = PrimaryGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Apply Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ====================================================================
// DETAIL ROW COMPONENT
// ====================================================================

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                label,
                color = TextGray,
                fontSize = 10.sp
            )
            Text(
                value,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}