package com.dark.jobai.ui.screens.main.applications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.dark.jobai.data.model.Application
import com.dark.jobai.data.model.Job
import com.dark.jobai.ui.components.EmptyState
import com.dark.jobai.ui.components.JobCard
import com.dark.jobai.util.Formatters
import com.dark.jobai.viewmodel.ApplicationsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onJobClick: (String) -> Unit = {}
) {
    val applicationsViewModel: ApplicationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val applications by applicationsViewModel.applications.collectAsState()
    val isLoading by applicationsViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedApplication by remember { mutableStateOf<Application?>(null) }

    // --- Saved Jobs State (Real-time Firestore sync) ---
    var savedJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoadingSavedJobs by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("saved_jobs")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        savedJobs = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Job::class.java)?.copy(id = doc.id)
                        }
                    }
                    isLoadingSavedJobs = false
                }
        } else {
            isLoadingSavedJobs = false
        }
    }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Application Pipeline",
                color = TextDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Live delivery status & dispatched transparency",
                color = AppBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Modern TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceWhite,
            contentColor = AppBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AppBlue,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = BorderSubtle) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "History (${applications.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                selectedContentColor = AppBlue,
                unselectedContentColor = TextMuted
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved (${savedJobs.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                selectedContentColor = AppBlue,
                unselectedContentColor = TextMuted
            )
        }

        when (selectedTab) {
            0 -> {
                AppliedTab(
                    applications = applications,
                    isLoading = isLoading,
                    onApplicationClick = { selectedApplication = it }
                )
            }
            1 -> {
                // Saved Jobs Tab
                if (isLoadingSavedJobs) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppBlue)
                    }
                } else if (savedJobs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.BookmarkBorder,
                            title = "No Saved Jobs",
                            description = "Jobs you save from the explore screen will appear here."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(savedJobs, key = { it.id }) { job ->
                            JobCard(
                                job = job,
                                onClick = { onJobClick(job.id) },
                                onEmailApply = {},
                                onSave = {
                                    if (currentUserId != null) {
                                        FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(currentUserId)
                                            .collection("saved_jobs")
                                            .document(job.id)
                                            .delete()
                                    }
                                },
                                isSaved = true,
                                isApplied = false
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    selectedApplication?.let { app ->
        ApplicationDetailSheet(
            application = app,
            onDismiss = { selectedApplication = null },
            onViewJobClick = {
                val jobId = app.jobId
                selectedApplication = null
                if (!jobId.isNullOrBlank()) {
                    onJobClick(jobId)
                }
            }
        )
    }
}

@Composable
fun AppliedTab(
    applications: List<Application>,
    isLoading: Boolean,
    onApplicationClick: (Application) -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)

    val totalCount = applications.size
    val openedCount = applications.count { it.openedAt > 0 || it.emailStatus.equals("Opened", ignoreCase = true) }
    val viewedCount = applications.count { it.clickedAt > 0 || it.emailStatus.equals("Viewed", ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Counter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Dispatched", totalCount.toString(), Icons.Default.Send, AppBlue, Modifier.weight(1f))
            StatCard("Opened", openedCount.toString(), Icons.Default.Visibility, WarningOrange, Modifier.weight(1f))
            StatCard("Reviewed", viewedCount.toString(), Icons.Default.Bolt, SuccessGreen, Modifier.weight(1f))
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            }
            applications.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Description,
                        title = "No Applications Yet",
                        description = "Apply to jobs to track recruiter status & sent details here."
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(applications, key = { it.id }) { application ->
                        ApplicationCard(
                            application = application,
                            onClick = { onApplicationClick(application) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Surface(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    onClick: () -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    val isWebForm = application.applicationType.equals("WebForm", ignoreCase = true)
    val isAuto = application.applicationType.equals("Auto", ignoreCase = true)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.jobTitle,
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = application.companyName,
                        color = AppBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(application = application)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Application Medium Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeColor = when {
                    isWebForm -> Color(0xFF8B5CF6)
                    isAuto -> Color(0xFF10B981)
                    else -> Color(0xFF0EA5E9)
                }
                val badgeText = when {
                    isWebForm -> "Applied via Website Portal"
                    isAuto -> "AI Auto-Applied"
                    else -> "Applied via Direct Email"
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isWebForm -> Icons.Default.Language
                                isAuto -> Icons.Default.SmartToy
                                else -> Icons.Default.Email
                            },
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Formatters.formatDate(application.appliedAt),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Tap to view details →",
                    color = AppBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatusBadge(application: Application) {
    val isOpened = application.openedAt > 0 || application.emailStatus.equals("Opened", ignoreCase = true)
    val isViewed = application.clickedAt > 0 || application.emailStatus.equals("Viewed", ignoreCase = true)
    val isDelivered = application.deliveredAt > 0 || application.emailStatus.equals("Delivered", ignoreCase = true)
    val isBounced = application.bouncedAt > 0 || application.emailStatus.equals("Bounced", ignoreCase = true)

    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val InfoBlue = Color(0xFF3B82F6)
    val ErrorRed = Color(0xFFEF4444)
    val TextMuted = Color(0xFF64748B)

    val (icon, color, label) = when {
        isViewed -> Triple(Icons.Default.Bolt, SuccessGreen, "Viewed")
        isOpened -> Triple(Icons.Default.Visibility, WarningOrange, "Opened")
        isDelivered -> Triple(Icons.Default.CheckCircle, InfoBlue, "Delivered")
        isBounced -> Triple(Icons.Default.Warning, ErrorRed, "Bounced")
        else -> Triple(Icons.Default.Send, TextMuted, "Sent")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailSheet(
    application: Application,
    onDismiss: () -> Unit,
    onViewJobClick: () -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val InfoBlue = Color(0xFF3B82F6)

    val isDelivered = application.deliveredAt > 0 || application.emailStatus.equals("Delivered", ignoreCase = true)
    val isOpened = application.openedAt > 0 || application.emailStatus.equals("Opened", ignoreCase = true)
    val isViewed = application.clickedAt > 0 || application.emailStatus.equals("Viewed", ignoreCase = true)

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
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Application Status",
                    color = AppBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                // Button to jump straight to the original job details!
                if (!application.jobId.isNullOrBlank()) {
                    TextButton(onClick = onViewJobClick) {
                        Text("View Job Post →", color = AppBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = application.jobTitle,
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = application.companyName,
                color = AppBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(16.dp))

            // ============ TRACKING TIMELINE ============
            Text(
                text = "Live Tracking Timeline",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimelineRow(
                icon = Icons.Default.Send,
                title = "Application Sent",
                time = Formatters.formatDateTime(application.appliedAt),
                color = TextMuted,
                isCompleted = true
            )

            TimelineRow(
                icon = Icons.Default.CheckCircle,
                title = "Email Delivered to Recruiter",
                time = if (isDelivered) (if (application.deliveredAt > 0) Formatters.formatDateTime(application.deliveredAt) else "Delivered to Recruiter Inbox ✓") else "Waiting for delivery...",
                color = InfoBlue,
                isCompleted = isDelivered
            )

            TimelineRow(
                icon = Icons.Default.Visibility,
                title = "Recruiter Opened Email",
                time = if (isOpened) (if (application.openedAt > 0) Formatters.formatDateTime(application.openedAt) else "Opened by Recruiter ✓") else "Waiting for recruiter to open...",
                color = WarningOrange,
                isCompleted = isOpened
            )

            TimelineRow(
                icon = Icons.Default.Bolt,
                title = "Resume Viewed",
                time = if (isViewed) (if (application.clickedAt > 0) Formatters.formatDateTime(application.clickedAt) else "Resume Reviewed ✓") else "Waiting for resume view...",
                color = SuccessGreen,
                isCompleted = isViewed
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(16.dp))

            // ============ TRANSPARENCY: WHAT DETAILS WERE SENT ============
            Text(
                text = "AI Dispatched Transparency",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    if (application.applicationType == "WebForm") {
                        // SHOW WEBSITE AUTO-FILL AUDIT LOG
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, null, tint = AppBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bot execution log for Company Portal:", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (application.botAuditLog.isEmpty()) {
                            Text("No recognizable input fields found on this specific portal.", fontSize = 11.sp, color = TextMuted)
                        } else {
                            application.botAuditLog.forEach { (field, value) ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• $field: ", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                    Text(value, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (application.botClickedSubmit) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (application.botClickedSubmit) SuccessGreen else WarningOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (application.botClickedSubmit) "Submit Button Detected & Clicked" else "Form Filled (Manual Submit needed)", fontSize = 11.sp, color = if (application.botClickedSubmit) SuccessGreen else WarningOrange, fontWeight = FontWeight.Bold)
                        }

                    } else {
                        // SHOW EMAIL LOG (Purana wala)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = AppBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Applicant: ${application.applicantName}", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = AppBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Target Email: ${application.toEmail}", fontSize = 12.sp, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineRow(
    icon: ImageVector,
    title: String,
    time: String,
    color: Color,
    isCompleted: Boolean
) {
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isCompleted) color.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (isCompleted) color.copy(alpha = 0.4f) else BorderSubtle)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) color else TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = if (isCompleted) TextDark else TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                color = if (isCompleted) color else TextMuted.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}