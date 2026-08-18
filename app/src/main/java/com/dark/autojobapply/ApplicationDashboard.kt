package com.dark.autojobapply

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.ui.theme.BackgroundDark
import com.dark.jobai.ui.theme.BorderGray
import com.dark.jobai.ui.theme.PrimaryGreen
import com.dark.jobai.ui.theme.TextGray
import com.dark.jobai.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ====================================================================
// APPLICATION DASHBOARD SCREEN
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDashboard(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var applications by remember { mutableStateOf<List<ApplicationRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedApplication by remember { mutableStateOf<ApplicationRecord?>(null) }
    var showEmailTemplate by remember { mutableStateOf(false) }
    var totalApplications by remember { mutableStateOf(0) }
    var openedCount by remember { mutableStateOf(0) }
    var deliveredCount by remember { mutableStateOf(0) }
    var clickedCount by remember { mutableStateOf(0) }

    // Fetch applications
    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(userId)
            .collection("applications")
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val appList = mutableListOf<ApplicationRecord>()

                    for (doc in snapshot.documents) {
                        val app = ApplicationRecord(
                            id = doc.id,
                            companyName = doc.getString("companyName") ?: "",
                            jobTitle = doc.getString("jobTitle") ?: "",
                            toEmail = doc.getString("toEmail") ?: "",
                            status = doc.getString("status") ?: "Applied",
                            emailStatus = doc.getString("emailStatus") ?: "Sent",
                            appliedAt = doc.getLong("appliedAt") ?: 0L,
                            deliveredAt = doc.getLong("deliveredAt") ?: 0L,
                            openedAt = doc.getLong("openedAt") ?: 0L,
                            clickedAt = doc.getLong("clickedAt") ?: 0L,
                            messageId = doc.getString("messageId") ?: "",
                            emailSubject = doc.getString("emailSubject") ?: "",
                            emailBody = doc.getString("emailBody") ?: ""
                        )
                        appList.add(app)
                    }

                    applications = appList
                    totalApplications = appList.size
                    deliveredCount = appList.count { it.deliveredAt > 0 }
                    openedCount = appList.count { it.openedAt > 0 }
                    clickedCount = appList.count { it.clickedAt > 0 }
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Application Dashboard",
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Track all your job applications",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Email Template Button
            IconButton(
                onClick = { showEmailTemplate = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
            ) {
                Icon(Icons.Default.Email, "Email Template", tint = PrimaryGreen)
            }
        }

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Total",
                value = totalApplications.toString(),
                icon = Icons.Default.Send,
                color = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Delivered",
                value = deliveredCount.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF42A5F5),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Opened",
                value = openedCount.toString(),
                icon = Icons.Default.Visibility,
                color = Color(0xFFFFA726),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Clicked",
                value = clickedCount.toString(),
                icon = Icons.Default.Link,
                color = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Application List
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            applications.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Applications Yet", color = TextGray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Apply to jobs to track them here",
                            color = TextGray.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(applications) { app ->
                        ApplicationCard(
                            application = app,
                            onClick = { selectedApplication = app }
                        )
                    }
                }
            }
        }
    }

    // Application Detail Bottom Sheet
    selectedApplication?.let { app ->
        ApplicationDetailSheet(
            application = app,
            onDismiss = { selectedApplication = null }
        )
    }

    // Email Template Sheet
    if (showEmailTemplate) {
        EmailTemplateSheet(
            onDismiss = { showEmailTemplate = false },
            onSaved = { showEmailTemplate = false }
        )
    }
}

// ====================================================================
// STAT CARD
// ====================================================================

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                color = TextGray,
                fontSize = 10.sp
            )
        }
    }
}

// ====================================================================
// APPLICATION CARD
// ====================================================================

@Composable
fun ApplicationCard(
    application: ApplicationRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        application.jobTitle,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        application.companyName,
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status Badge
                StatusBadge(application = application)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Email info
            if (application.toEmail.isNotEmpty()) {
                Text(
                    "📧 ${application.toEmail}",
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Applied: ${formatDate(application.appliedAt)}",
                    color = TextGray.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )

                if (application.openedAt > 0) {
                    Text(
                        "Opened: ${formatDate(application.openedAt)}",
                        color = Color(0xFFFFA726),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ====================================================================
// STATUS BADGE
// ====================================================================

@Composable
fun StatusBadge(application: ApplicationRecord) {
    val (icon, color, label) = when {
        application.clickedAt > 0 -> Triple(
            Icons.Default.Link,
            Color(0xFF66BB6A),
            "Resume Viewed"
        )
        application.openedAt > 0 -> Triple(
            Icons.Default.Visibility,
            Color(0xFFFFA726),
            "Opened"
        )
        application.deliveredAt > 0 -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF42A5F5),
            "Delivered"
        )
        else -> Triple(
            Icons.Default.Send,
            TextGray,
            "Sent"
        )
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ====================================================================
// APPLICATION DETAIL SHEET
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailSheet(
    application: ApplicationRecord,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Text(
                application.jobTitle,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                application.companyName,
                color = PrimaryGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Email Status Timeline
            Text("Email Status", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            // Applied
            TimelineItem(
                icon = Icons.Default.Send,
                title = "Application Sent",
                time = formatDate(application.appliedAt),
                color = TextGray,
                isCompleted = true
            )

            // Delivered
            TimelineItem(
                icon = Icons.Default.CheckCircle,
                title = "Email Delivered",
                time = if (application.deliveredAt > 0) formatDate(application.deliveredAt) else "Pending",
                color = Color(0xFF42A5F5),
                isCompleted = application.deliveredAt > 0
            )

            // Opened
            TimelineItem(
                icon = Icons.Default.Visibility,
                title = "Opened by HR",
                time = if (application.openedAt > 0) formatDate(application.openedAt) else "Pending",
                color = Color(0xFFFFA726),
                isCompleted = application.openedAt > 0
            )

            // Clicked
            TimelineItem(
                icon = Icons.Default.Link,
                title = "Resume Viewed",
                time = if (application.clickedAt > 0) formatDate(application.clickedAt) else "Pending",
                color = Color(0xFF66BB6A),
                isCompleted = application.clickedAt > 0
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Email Details
            Text("Email Details", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (application.toEmail.isNotEmpty()) {
                Text("To: ${application.toEmail}", color = TextWhite, fontSize = 13.sp)
            }

            if (application.emailSubject.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Subject: ${application.emailSubject}", color = TextGray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ====================================================================
// TIMELINE ITEM
// ====================================================================

@Composable
fun TimelineItem(
    icon: ImageVector,
    title: String,
    time: String,
    color: Color,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isCompleted) color.copy(alpha = 0.2f) else Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isCompleted) color else TextGray.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isCompleted) TextWhite else TextGray.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                time,
                color = if (isCompleted) color else TextGray.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

// ====================================================================
// EMAIL TEMPLATE SHEET
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTemplateSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val emailManager = remember { EmailApplicationManager(context) }
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("Application for {job_title} at {company}") }
    var body by remember { mutableStateOf("""Dear Hiring Manager,

My name is {applicant_name}. I am applying for the {job_title} position at {company}.

I believe my skills and experience make me a strong candidate.

Best regards,
{applicant_name}""") }

    var autoSend by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Email Template",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subject
            Text("Subject", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = Color(0xFF0D0D0D),
                    unfocusedContainerColor = Color(0xFF0D0D0D)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body
            Text("Body", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = Color(0xFF0D0D0D),
                    unfocusedContainerColor = Color(0xFF0D0D0D)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-send toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Send Mode", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Email sent automatically", color = TextGray, fontSize = 12.sp)
                }
                Switch(
                    checked = autoSend,
                    onCheckedChange = { autoSend = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = PrimaryGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        emailManager.saveTemplateToServer(
                            subjectTemplate = subject,
                            bodyTemplate = body,
                            autoSend = autoSend,
                            onSuccess = {
                                isSaving = false
                                Toast.makeText(context, "✅ Template saved!", Toast.LENGTH_SHORT).show()
                                onSaved()
                            },
                            onError = { error ->
                                isSaving = false
                                Toast.makeText(context, "❌ $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Template", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ====================================================================
// APPLICATION RECORD DATA CLASS
// ====================================================================



// ====================================================================
// HELPER
// ====================================================================

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return format.format(date)
}