package com.dark.jobai.ui.screens.main.jobs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.data.model.Job
import com.dark.jobai.data.repository.EmailRepository
import com.dark.jobai.ui.components.EmptyState
import com.dark.jobai.ui.components.ErrorState
import com.dark.jobai.ui.components.JobCard
import com.dark.jobai.viewmodel.JobsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    onJobClick: (String) -> Unit = {},
    onEmailTemplateNeeded: () -> Unit = {},
    onProfileNeeded: () -> Unit = {}
) {
    val context = LocalContext.current
    val jobsViewModel: JobsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val filteredJobs by jobsViewModel.filteredJobs.collectAsState()
    val isLoading by jobsViewModel.isLoading.collectAsState()
    val errorMessage by jobsViewModel.errorMessage.collectAsState()
    val searchQuery by jobsViewModel.searchQuery.collectAsState()
    val workTypeFilter by jobsViewModel.workTypeFilter.collectAsState()
    val showEmailOnly by jobsViewModel.showEmailOnly.collectAsState()
    val savedJobIds by jobsViewModel.savedJobIds.collectAsState()

    // --- State for tracking applied jobs ---
    var appliedJobIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isEmailSending by remember { mutableStateOf(false) }

    // Fetch applied job IDs from user's subcollection in real-time
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("applications")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val ids = snapshot.documents.mapNotNull { it.getString("jobId") }.toSet()
                        appliedJobIds = ids
                    }
                }
        }
    }

    fun handleEmailApply(job: Job) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        scope.launch {
            val userDoc = try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }

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
                toEmail = job.contactEmail,
                jobTitle = job.title,
                companyName = job.company
            )

            result.onSuccess {
                isEmailSending = false
                Toast.makeText(context, "✅ Email sent successfully!", Toast.LENGTH_LONG).show()
                appliedJobIds = appliedJobIds + job.id // Instantly update UI to Applied
            }.onFailure { e ->
                isEmailSending = false
                Toast.makeText(context, "❌ ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        topBar = {
            Column(
                modifier = Modifier
                    .background(BgLight)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Top Row: Headline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Find Your Next Role",
                            color = TextDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "AI-Powered Job Matching",
                            color = AppBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        IconButton(
                            onClick = { jobsViewModel.loadJobs() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = TextDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { jobsViewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search jobs, companies, skills...",
                            color = TextMuted,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { jobsViewModel.updateSearchQuery("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AppBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        cursorColor = AppBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption("All", workTypeFilter == "All") { jobsViewModel.updateWorkTypeFilter("All") }
                    FilterOption("Remote", workTypeFilter == "Remote") { jobsViewModel.updateWorkTypeFilter("Remote") }
                    FilterOption("Hybrid", workTypeFilter == "Hybrid") { jobsViewModel.updateWorkTypeFilter("Hybrid") }
                    FilterOption("Direct Apply", showEmailOnly) { jobsViewModel.toggleEmailOnly(!showEmailOnly) }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppBlue)
                    }
                }
                errorMessage != null -> ErrorState(message = errorMessage ?: "", onRetry = { jobsViewModel.loadJobs() })
                filteredJobs.isEmpty() -> EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No Jobs Found",
                    description = "Try adjusting your search or filters to see more results."
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgLight),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredJobs.size} matching opportunities",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    items(filteredJobs, key = { it.id }) { job ->
                        JobCard(
                            job = job,
                            onClick = { onJobClick(job.id) },
                            onEmailApply = { handleEmailApply(job) },
                            onSave = {
                                if (currentUserId != null) {
                                    val dbRef = FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(currentUserId)
                                        .collection("saved_jobs")
                                        .document(job.id)

                                    if (job.id in savedJobIds) {
                                        // Remove from Local & Firestore
                                        jobsViewModel.removeSavedJob(job.id)
                                        dbRef.delete()
                                        Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Add to Local & Firestore
                                        jobsViewModel.saveJob(job)
                                        dbRef.set(job)
                                        Toast.makeText(context, "Job saved!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please login to save jobs", Toast.LENGTH_SHORT).show()
                                }
                            },
                            isSaved = job.id in savedJobIds,
                            isApplied = job.id in appliedJobIds // <--- Now fully resolved!
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            if (isEmailSending) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = AppBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Applying via AI...", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AppBlue else SurfaceWhite,
        border = BorderStroke(1.dp, if (isSelected) AppBlue else BorderSubtle),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}