package com.dark.autojobapply

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.dark.autojobapply.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ====================================================================
// MAIN HOME SCREEN - FINAL COMPLETE VERSION
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    onLogout: () -> Unit = {},
    onProfileNeeded: () -> Unit = {},
    onEmailTemplateNeeded: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val emailManager = remember { EmailApplicationManager(context) }

    // Navigation state
    var selectedTab by remember { mutableStateOf(0) }

    // Job list state
    var searchQuery by remember { mutableStateOf("") }
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var filteredJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var workTypeFilter by remember { mutableStateOf("All") }
    var showEmailOnly by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch jobs from Firestore
    LaunchedEffect(Unit) {
        db.collection("jobs")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    errorMessage = error.localizedMessage ?: "Failed to load jobs"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val jobList = mutableListOf<Job>()
                    for (doc in snapshot.documents) {
                        try {
                            val job = Job.fromDocument(doc)
                            jobList.add(job)
                        } catch (e: Exception) {
                            // Skip malformed documents
                        }
                    }

                    // Sort: Email jobs first, then latest
                    jobList.sortWith(
                        compareByDescending<Job> { it.hasEmail }
                            .thenByDescending { it.postedAt }
                    )

                    jobs = jobList
                    filteredJobs = applyFilters(jobList, searchQuery, workTypeFilter, showEmailOnly)
                    isLoading = false
                }
            }
    }

    // Check profile before applying
    fun checkProfileAndApply(job: Job) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profileCompleted = doc.getBoolean("profileCompleted") ?: false

                    if (profileCompleted) {
                        // Profile complete - Open apply URL
                        if (job.applyUrl.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applyUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Apply link not available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Profile not complete - Direct to Profile Setup
                        onProfileNeeded()
                    }
                } else {
                    // User document doesn't exist - Direct to Profile Setup
                    onProfileNeeded()
                }
            }
            .addOnFailureListener { e ->
                // Error - Direct to Profile Setup
                onProfileNeeded()
            }
    }

    // Email apply handler
    fun handleEmailApply(job: Job) {
        if (!emailManager.hasEmailApp()) {
            Toast.makeText(context, "No email app installed", Toast.LENGTH_SHORT).show()
            return
        }

        if (!emailManager.isTemplateSaved()) {
            // First time - go to email template setup
            onEmailTemplateNeeded()
            return
        }

        if (emailManager.isAutoSendEnabled()) {
            // Auto-send mode - one click direct
            emailManager.sendEmailDirectly(
                toEmail = job.contactEmail,
                jobTitle = job.title,
                companyName = job.company,
                onSuccess = {
                    Toast.makeText(context, "Email opened!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Edit mode - open with editable content
            emailManager.openEmailWithEdit(
                toEmail = job.contactEmail,
                jobTitle = job.title,
                companyName = job.company,
                onSuccess = {
                    Toast.makeText(context, "Email opened for editing", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D0D0D),
                contentColor = TextWhite
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved") },
                    label = { Text("Saved", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    isLoading = isLoading,
                    jobs = filteredJobs,
                    errorMessage = errorMessage,
                    searchQuery = searchQuery,
                    onSearchChange = { query ->
                        searchQuery = query
                        filteredJobs = applyFilters(jobs, query, workTypeFilter, showEmailOnly)
                    },
                    workTypeFilter = workTypeFilter,
                    onWorkTypeFilterChange = { filter ->
                        workTypeFilter = filter
                        filteredJobs = applyFilters(jobs, searchQuery, filter, showEmailOnly)
                    },
                    showEmailOnly = showEmailOnly,
                    onEmailOnlyToggle = { emailOnly ->
                        showEmailOnly = emailOnly
                        filteredJobs = applyFilters(jobs, searchQuery, workTypeFilter, emailOnly)
                    },
                    onJobClick = { job -> checkProfileAndApply(job) },
                    onEmailApply = { job -> handleEmailApply(job) },
                    onSaveJob = { job ->
                        saveJobForUser(db, auth.currentUser?.uid, job)
                        Toast.makeText(context, "Job saved!", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> SavedJobsTab(
                    onJobClick = { job -> checkProfileAndApply(job) },
                    onEmailApply = { job -> handleEmailApply(job) }
                )
                2 -> ProfileTab(onLogout = onLogout)
            }
        }
    }
}

// ====================================================================
// FILTER FUNCTION
// ====================================================================

private fun applyFilters(
    allJobs: List<Job>,
    query: String,
    workType: String,
    emailOnly: Boolean
): List<Job> {
    return allJobs.filter { job ->
        val matchesSearch = query.isBlank() ||
                job.title.contains(query, ignoreCase = true) ||
                job.company.contains(query, ignoreCase = true) ||
                job.location.contains(query, ignoreCase = true)

        val matchesWorkType = workType == "All" || job.workType == workType
        val matchesEmail = !emailOnly || job.hasEmail

        matchesSearch && matchesWorkType && matchesEmail
    }
}

// ====================================================================
// HOME TAB
// ====================================================================

@Composable
fun HomeTab(
    isLoading: Boolean,
    jobs: List<Job>,
    errorMessage: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    workTypeFilter: String,
    onWorkTypeFilterChange: (String) -> Unit,
    showEmailOnly: Boolean,
    onEmailOnlyToggle: (Boolean) -> Unit,
    onJobClick: (Job) -> Unit,
    onEmailApply: (Job) -> Unit,
    onSaveJob: (Job) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Find Your Dream Job",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${jobs.size} jobs available",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            placeholder = { Text("Search jobs...", color = TextGray.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextGray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = TextGray)
                    }
                }
            },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A)
            ),
            singleLine = true
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Remote", "Hybrid", "On-site").forEach { filter ->
                FilterChip(
                    selected = workTypeFilter == filter,
                    onClick = { onWorkTypeFilterChange(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (workTypeFilter == filter) PrimaryGreen else Color(0xFF1A1A1A),
                        labelColor = if (workTypeFilter == filter) Color.Black else TextGray
                    )
                )
            }
        }

        // Email Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show jobs with direct email", color = TextGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = showEmailOnly,
                onCheckedChange = onEmailOnlyToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = BorderGray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Error loading jobs", color = ErrorRed, fontSize = 16.sp)
                        Text(errorMessage, color = TextGray, fontSize = 12.sp)
                    }
                }
            }
            jobs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No jobs found", color = TextGray, fontSize = 16.sp)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs) { job ->
                        JobCard(
                            job = job,
                            onClick = { onJobClick(job) },
                            onEmailApply = { onEmailApply(job) },
                            onSave = { onSaveJob(job) }
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// JOB CARD - With Email Apply Button
// ====================================================================

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    onSave: () -> Unit,
    onEmailApply: () -> Unit = {}
) {
    var isSaved by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(
            1.dp,
            if (job.hasEmail) PrimaryGreen.copy(alpha = 0.4f) else BorderGray.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Company Logo
                    if (job.companyLogo.isNotEmpty()) {
                        AsyncImage(
                            model = job.companyLogo,
                            contentDescription = "Logo",
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(job.company.take(1).uppercase(), color = PrimaryGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(job.title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(job.company, color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                IconButton(onClick = { isSaved = !isSaved; onSave() }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) ErrorRed else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoTag(Icons.Default.LocationOn, job.location)
                InfoTag(Icons.Default.Work, job.workType)
                if (job.salary != "Not Disclosed") {
                    InfoTag(Icons.Default.Payments, job.salary)
                }
            }

            // Email indicator
            if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Direct email available", color = PrimaryGreen, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Date + Email Button + Apply Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatTimeAgo(job.postedAt), color = TextGray.copy(alpha = 0.6f), fontSize = 10.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Email Apply Button - Only if email available
                    if (job.hasEmail && job.contactEmail.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onEmailApply,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                            border = BorderStroke(1.dp, PrimaryGreen),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email Apply",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Regular Apply Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ====================================================================
// INFO TAG
// ====================================================================

@Composable
fun InfoTag(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF2A2A2A)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextGray, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = TextGray, fontSize = 10.sp)
    }
}

// ====================================================================
// SAVED JOBS TAB
// ====================================================================

@Composable
fun SavedJobsTab(
    onJobClick: (Job) -> Unit = {},
    onEmailApply: (Job) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var savedJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(userId)
            .collection("savedJobs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val jobList = mutableListOf<Job>()
                    for (doc in snapshot.documents) {
                        try {
                            val job = Job.fromDocument(doc)
                            jobList.add(job)
                        } catch (e: Exception) {}
                    }
                    savedJobs = jobList
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        Text(
            text = "Saved Jobs",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            savedJobs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Saved Jobs", color = TextGray, fontSize = 16.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedJobs) { job ->
                        JobCard(
                            job = job,
                            onClick = { onJobClick(job) },
                            onEmailApply = { onEmailApply(job) },
                            onSave = { /* Already saved */ }
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// PROFILE TAB
// ====================================================================

@Composable
fun ProfileTab(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(
                Brush.linearGradient(listOf(PrimaryGreen, Color(0xFF00D2A0)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Text((user?.displayName ?: "U").take(1).uppercase(), color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.displayName ?: "User", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", color = TextGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { auth.signOut(); onLogout() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ====================================================================
// HELPERS
// ====================================================================

private fun saveJobForUser(db: FirebaseFirestore, userId: String?, job: Job) {
    if (userId == null) return
    db.collection("users").document(userId)
        .collection("savedJobs").document(job.id)
        .set(job.toMap())
}

private fun formatTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return "Recently"
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}