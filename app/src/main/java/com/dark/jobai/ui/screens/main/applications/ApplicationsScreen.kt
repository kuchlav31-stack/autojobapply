package com.dark.jobai.ui.screens.main.applications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val PageBackground = Color(0xFFF7F8FA)
private val CardWhite = Color(0xFFFFFFFF)
private val PrimaryBlue = Color(0xFF2563EB)
private val PrimaryBlueSoft = Color(0xFFEFF4FF)
private val TextMain = Color(0xFF172033)
private val TextSecondary = Color(0xFF6B7280)
private val TextLight = Color(0xFF9CA3AF)
private val DividerColor = Color(0xFFE8EBF0)

private val Green = Color(0xFF15803D)
private val GreenSoft = Color(0xFFECFDF3)

private val Orange = Color(0xFFB45309)
private val OrangeSoft = Color(0xFFFFF7E8)

private val Red = Color(0xFFB91C1C)
private val RedSoft = Color(0xFFFEF2F2)

private val Purple = Color(0xFF6D28D9)
private val PurpleSoft = Color(0xFFF5F3FF)

private val CardRadius = RoundedCornerShape(16.dp)
private val SmallRadius = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onJobClick: (String) -> Unit = {}
) {
    val applicationsViewModel: ApplicationsViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val applications by applicationsViewModel.applications.collectAsState()
    val isLoading by applicationsViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedApplication by remember {
        mutableStateOf<Application?>(null)
    }

    var savedJobs by remember {
        mutableStateOf<List<Job>>(emptyList())
    }

    var isLoadingSavedJobs by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("saved_jobs")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        savedJobs = snapshot.documents.mapNotNull { document ->
                            document.toObject(Job::class.java)
                                ?.copy(id = document.id)
                        }
                    }

                    isLoadingSavedJobs = false
                }
        } else {
            isLoadingSavedJobs = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        ApplicationsTopBar()

        ApplicationsTabs(
            selectedTab = selectedTab,
            applicationsCount = applications.size,
            savedJobsCount = savedJobs.size,
            onTabSelected = { selectedTab = it }
        )

        when (selectedTab) {
            0 -> {
                ApplicationsHistory(
                    applications = applications,
                    isLoading = isLoading,
                    onApplicationClick = {
                        selectedApplication = it
                    }
                )
            }

            1 -> {
                SavedJobsContent(
                    savedJobs = savedJobs,
                    isLoading = isLoadingSavedJobs,
                    currentUserId = currentUserId,
                    onJobClick = onJobClick
                )
            }
        }
    }

    selectedApplication?.let { application ->
        ApplicationDetailsSheet(
            application = application,
            onDismiss = {
                selectedApplication = null
            },
            onViewJobClick = {
                val jobId = application.jobId
                selectedApplication = null

                if (!jobId.isNullOrBlank()) {
                    onJobClick(jobId)
                }
            }
        )
    }
}

@Composable
private fun ApplicationsTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {
        Text(
            text = "Applications",
            color = TextMain,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Track your job applications",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ApplicationsTabs(
    selectedTab: Int,
    applicationsCount: Int,
    savedJobsCount: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = CardWhite,
        contentColor = PrimaryBlue,
        divider = {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp
            )
        },
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    tabPositions[selectedTab]
                ),
                color = PrimaryBlue,
                height = 2.dp
            )
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
            },
            selectedContentColor = PrimaryBlue,
            unselectedContentColor = TextSecondary,
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Applied $applicationsCount",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 0) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            }
        )

        Tab(
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            },
            selectedContentColor = PrimaryBlue,
            unselectedContentColor = TextSecondary,
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Saved $savedJobsCount",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 1) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun ApplicationsHistory(
    applications: List<Application>,
    isLoading: Boolean,
    onApplicationClick: (Application) -> Unit
) {
    when {
        isLoading -> {
            LoadingContent(
                text = "Loading applications..."
            )
        }

        applications.isEmpty() -> {
            EmptyContent(
                icon = Icons.Default.Description,
                title = "No applications yet",
                description = "Your submitted applications will appear here."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Recent applications",
                        color = TextMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = 2.dp,
                            bottom = 2.dp
                        )
                    )
                }

                items(
                    items = applications,
                    key = { it.id }
                ) { application ->
                    ApplicationItem(
                        application = application,
                        onClick = {
                            onApplicationClick(application)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SavedJobsContent(
    savedJobs: List<Job>,
    isLoading: Boolean,
    currentUserId: String?,
    onJobClick: (String) -> Unit
) {
    when {
        isLoading -> {
            LoadingContent(
                text = "Loading saved jobs..."
            )
        }

        savedJobs.isEmpty() -> {
            EmptyContent(
                icon = Icons.Default.BookmarkBorder,
                title = "No saved jobs",
                description = "Jobs you save from Explore will appear here."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = savedJobs,
                    key = { it.id }
                ) { job ->
                    JobCard(
                        job = job,
                        onClick = {
                            onJobClick(job.id)
                        },
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

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ApplicationItem(
    application: Application,
    onClick: () -> Unit
) {
    val applicationType = application.applicationType

    val typeText = when {
        applicationType.equals("WebForm", ignoreCase = true) ->
            "Website application"

        applicationType.equals("Auto", ignoreCase = true) ->
            "AI auto-applied"

        else ->
            "Direct email"
    }

    val typeIcon = when {
        applicationType.equals("WebForm", ignoreCase = true) ->
            Icons.Default.Language

        applicationType.equals("Auto", ignoreCase = true) ->
            Icons.Default.SmartToy

        else ->
            Icons.Default.Email
    }

    val typeColor = when {
        applicationType.equals("WebForm", ignoreCase = true) ->
            Purple

        applicationType.equals("Auto", ignoreCase = true) ->
            Green

        else ->
            PrimaryBlue
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardRadius,
        color = CardWhite,
        border = BorderStroke(
            width = 1.dp,
            color = DividerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                CompanyAvatar(
                    companyName = application.companyName
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = application.jobTitle,
                        color = TextMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = application.companyName,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                ApplicationStatus(
                    application = application
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = typeText,
                    color = typeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = Formatters.formatDate(application.appliedAt),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    tint = TextLight,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun CompanyAvatar(
    companyName: String
) {
    val initial = companyName
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "C"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PrimaryBlueSoft
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = PrimaryBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ApplicationStatus(
    application: Application
) {
    val isViewed = application.clickedAt > 0 ||
            application.emailStatus.equals(
                "Viewed",
                ignoreCase = true
            )

    val isOpened = application.openedAt > 0 ||
            application.emailStatus.equals(
                "Opened",
                ignoreCase = true
            )

    val isDelivered = application.deliveredAt > 0 ||
            application.emailStatus.equals(
                "Delivered",
                ignoreCase = true
            )

    val isBounced = application.bouncedAt > 0 ||
            application.emailStatus.equals(
                "Bounced",
                ignoreCase = true
            )

    val status = when {
        isViewed -> StatusData(
            label = "Viewed",
            icon = Icons.Default.Visibility,
            color = Green,
            background = GreenSoft
        )

        isOpened -> StatusData(
            label = "Opened",
            icon = Icons.Default.Visibility,
            color = Orange,
            background = OrangeSoft
        )

        isBounced -> StatusData(
            label = "Bounced",
            icon = Icons.Default.ErrorOutline,
            color = Red,
            background = RedSoft
        )

        isDelivered -> StatusData(
            label = "Delivered",
            icon = Icons.Default.CheckCircle,
            color = PrimaryBlue,
            background = PrimaryBlueSoft
        )

        else -> StatusData(
            label = "Sent",
            icon = Icons.Default.Send,
            color = TextSecondary,
            background = Color(0xFFF3F4F6)
        )
    }

    Surface(
        shape = SmallRadius,
        color = status.background
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
                modifier = Modifier.size(13.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = status.label,
                color = status.color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class StatusData(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val background: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDetailsSheet(
    application: Application,
    onDismiss: () -> Unit,
    onViewJobClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isDelivered = application.deliveredAt > 0 ||
            application.emailStatus.equals(
                "Delivered",
                ignoreCase = true
            )

    val isOpened = application.openedAt > 0 ||
            application.emailStatus.equals(
                "Opened",
                ignoreCase = true
            )

    val isViewed = application.clickedAt > 0 ||
            application.emailStatus.equals(
                "Viewed",
                ignoreCase = true
            )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        contentColor = TextMain,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 30.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Application details",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Application activity and delivery status",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    modifier = Modifier.clickable(onClick = onDismiss),
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardRadius,
                color = PageBackground
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompanyAvatar(
                        companyName = application.companyName
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = application.jobTitle,
                            color = TextMain,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = application.companyName,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (!application.jobId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onViewJobClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = SmallRadius,
                    border = BorderStroke(
                        width = 1.dp,
                        color = PrimaryBlue
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = "View original job",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailHeading(
                title = "Application timeline"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardRadius,
                color = PageBackground
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 8.dp
                    )
                ) {
                    TimelineItem(
                        icon = Icons.Default.Send,
                        title = "Application sent",
                        value = Formatters.formatDateTime(
                            application.appliedAt
                        ),
                        isCompleted = true,
                        color = PrimaryBlue,
                        isLast = false
                    )

                    TimelineItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Email delivered",
                        value = if (isDelivered) {
                            if (application.deliveredAt > 0) {
                                Formatters.formatDateTime(
                                    application.deliveredAt
                                )
                            } else {
                                "Delivered to recruiter"
                            }
                        } else {
                            "Waiting for delivery"
                        },
                        isCompleted = isDelivered,
                        color = PrimaryBlue,
                        isLast = false
                    )

                    TimelineItem(
                        icon = Icons.Default.Visibility,
                        title = "Recruiter opened email",
                        value = if (isOpened) {
                            if (application.openedAt > 0) {
                                Formatters.formatDateTime(
                                    application.openedAt
                                )
                            } else {
                                "Opened by recruiter"
                            }
                        } else {
                            "Waiting for recruiter"
                        },
                        isCompleted = isOpened,
                        color = Orange,
                        isLast = false
                    )

                    TimelineItem(
                        icon = Icons.Default.Check,
                        title = "Resume viewed",
                        value = if (isViewed) {
                            if (application.clickedAt > 0) {
                                Formatters.formatDateTime(
                                    application.clickedAt
                                )
                            } else {
                                "Resume viewed"
                            }
                        } else {
                            "Waiting for resume view"
                        },
                        isCompleted = isViewed,
                        color = Green,
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailHeading(
                title = "Dispatch information"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DispatchInformation(
                application = application
            )
        }
    }
}

@Composable
private fun DetailHeading(
    title: String
) {
    Text(
        text = title,
        color = TextMain,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun TimelineItem(
    icon: ImageVector,
    title: String,
    value: String,
    isCompleted: Boolean,
    color: Color,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCompleted) {
                    color.copy(alpha = 0.12f)
                } else {
                    Color(0xFFE5E7EB)
                }
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) {
                            color
                        } else {
                            TextLight
                        },
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(25.dp)
                        .background(
                            if (isCompleted) {
                                color.copy(alpha = 0.25f)
                            } else {
                                DividerColor
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp)
        ) {
            Text(
                text = title,
                color = if (isCompleted) {
                    TextMain
                } else {
                    TextSecondary
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = if (isCompleted) {
                    color
                } else {
                    TextLight
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            if (!isLast) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun DispatchInformation(
    application: Application
) {
    val isWebForm = application.applicationType.equals(
        "WebForm",
        ignoreCase = true
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardRadius,
        color = PageBackground
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            if (isWebForm) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Website form activity",
                        color = TextMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (application.botAuditLog.isEmpty()) {
                    Text(
                        text = "No recognizable fields were found on this portal.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    application.botAuditLog.forEach { (field, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = field,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = value,
                                color = TextMain,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    color = DividerColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (application.botClickedSubmit) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.ErrorOutline
                        },
                        contentDescription = null,
                        tint = if (application.botClickedSubmit) {
                            Green
                        } else {
                            Orange
                        },
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (application.botClickedSubmit) {
                            "Submit button clicked"
                        } else {
                            "Form filled, manual submission may be required"
                        },
                        color = if (application.botClickedSubmit) {
                            Green
                        } else {
                            Orange
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                DispatchInfoRow(
                    icon = Icons.Default.Person,
                    label = "Applicant",
                    value = application.applicantName
                )

                Spacer(modifier = Modifier.height(14.dp))

                DispatchInfoRow(
                    icon = Icons.Default.Email,
                    label = "Target email",
                    value = application.toEmail
                )
            }
        }
    }
}

@Composable
private fun DispatchInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                color = TextLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                color = TextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LoadingContent(
    text: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = PrimaryBlue,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = text,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmptyContent(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            icon = icon,
            title = title,
            description = description
        )
    }
}