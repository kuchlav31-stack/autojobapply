package com.dark.jobai.ui.screens.main.jobs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.data.model.Job
import com.dark.jobai.data.repository.EmailRepository
import com.dark.jobai.ui.components.EmptyState
import com.dark.jobai.ui.components.ErrorState
import com.dark.jobai.ui.components.FullScreenLoading
import com.dark.jobai.ui.components.JobCard
import com.dark.jobai.ui.components.SearchBar
import com.dark.jobai.ui.theme.*
import com.dark.jobai.viewmodel.JobsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun JobsScreen(
    onJobClick: (String) -> Unit = {},
    onEmailTemplateNeeded: () -> Unit = {},
    onProfileNeeded: () -> Unit = {}
) {
    val context = LocalContext.current
    val jobsViewModel: JobsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scope = rememberCoroutineScope()

    val filteredJobs by jobsViewModel.filteredJobs.collectAsState()
    val isLoading by jobsViewModel.isLoading.collectAsState()
    val errorMessage by jobsViewModel.errorMessage.collectAsState()
    val searchQuery by jobsViewModel.searchQuery.collectAsState()
    val workTypeFilter by jobsViewModel.workTypeFilter.collectAsState()
    val showEmailOnly by jobsViewModel.showEmailOnly.collectAsState()
    val savedJobIds by jobsViewModel.savedJobIds.collectAsState()

    var isEmailSending by remember { mutableStateOf(false) }

    // Email apply handler
    fun handleEmailApply(job: Job) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        scope.launch {
            // Check user document
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

            // Check email template
            val hasTemplate = userDoc.getString("emailSubjectTemplate")?.isNotEmpty() == true

            if (!hasTemplate) {
                onEmailTemplateNeeded()
                return@launch
            }

            // Send email
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
            }.onFailure { e ->
                isEmailSending = false
                Toast.makeText(context, "❌ ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Find Your Dream Job",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${filteredJobs.size} jobs available",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { jobsViewModel.loadJobs() },
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceDark, RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.Default.Refresh, "Refresh", tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
        }

        // Search
        SearchBar(
            query = searchQuery,
            onQueryChange = { jobsViewModel.updateSearchQuery(it) },
            placeholder = "Search jobs...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Remote", "Hybrid", "On-site").forEach { filter ->
                FilterChip(
                    selected = workTypeFilter == filter,
                    onClick = { jobsViewModel.updateWorkTypeFilter(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (workTypeFilter == filter) PrimaryGreen else SurfaceDark,
                        labelColor = if (workTypeFilter == filter) Color.Black else TextGray
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }

        // Email toggle
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
                onCheckedChange = { jobsViewModel.toggleEmailOnly(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = PrimaryGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            isLoading -> FullScreenLoading("Loading jobs...")
            errorMessage != null -> ErrorState(
                message = errorMessage ?: "",
                onRetry = { jobsViewModel.loadJobs() }
            )
            filteredJobs.isEmpty() -> EmptyState(
                icon = Icons.Default.Search,
                title = "No jobs found",
                description = "Try adjusting your filters"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredJobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job.id) },
                        onEmailApply = { handleEmailApply(job) },
                        onSave = {
                            if (job.id in savedJobIds) {
                                jobsViewModel.removeSavedJob(job.id)
                            } else {
                                jobsViewModel.saveJob(job)
                            }
                        },
                        isSaved = job.id in savedJobIds
                    )
                }
            }
        }
    }
}