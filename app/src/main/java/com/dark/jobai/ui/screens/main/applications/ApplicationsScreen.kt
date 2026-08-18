package com.dark.jobai.ui.screens.main.applications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.data.model.Application
import com.dark.jobai.data.repository.ApplicationStats
import com.dark.jobai.ui.components.EmptyState
import com.dark.jobai.ui.theme.*
import com.dark.jobai.util.Formatters
import com.dark.jobai.viewmodel.ApplicationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen() {
    val applicationsViewModel: ApplicationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val applications by applicationsViewModel.applications.collectAsState()
    val stats by applicationsViewModel.stats.collectAsState()
    val isLoading by applicationsViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedApplication by remember { mutableStateOf<Application?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Text(
            "My Applications",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = PrimaryGreen
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Applied (${applications.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Saved", fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> {
                AppliedTab(
                    applications = applications,
                    stats = stats,
                    isLoading = isLoading,
                    onApplicationClick = { selectedApplication = it }
                )
            }
            1 -> {
                EmptyState(
                    icon = Icons.Default.FavoriteBorder,
                    title = "No Saved Jobs",
                    description = "Jobs you save will appear here"
                )
            }
        }
    }

    selectedApplication?.let { app ->
        ApplicationDetailSheet(
            application = app,
            onDismiss = { selectedApplication = null }
        )
    }
}

@Composable
fun AppliedTab(
    applications: List<Application>,
    stats: ApplicationStats,
    isLoading: Boolean,
    onApplicationClick: (Application) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Total", stats.total.toString(), Icons.Default.Send, PrimaryGreen, Modifier.weight(1f))
            StatCard("Delivered", stats.delivered.toString(), Icons.Default.CheckCircle, InfoBlue, Modifier.weight(1f))
            StatCard("Opened", stats.opened.toString(), Icons.Default.Visibility, WarningOrange, Modifier.weight(1f))
            StatCard("Clicked", stats.clicked.toString(), Icons.Default.Link, SuccessGreen, Modifier.weight(1f))
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            applications.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Applications Yet", color = TextGray, fontSize = 16.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(applications, key = { it.id }) { application ->
                        ApplicationCard(
                            application = application,
                            onClick = { onApplicationClick(application) }
                        )
                    }
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextGray, fontSize = 9.sp)
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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

                StatusBadge(application = application)
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            Text(
                "Applied: ${Formatters.formatDate(application.appliedAt)}",
                color = TextGray.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StatusBadge(application: Application) {
    val (icon, color, label) = when {
        application.clickedAt > 0 -> Triple(Icons.Default.Link, SuccessGreen, "Viewed")
        application.openedAt > 0 -> Triple(Icons.Default.Visibility, WarningOrange, "Opened")
        application.deliveredAt > 0 -> Triple(Icons.Default.CheckCircle, InfoBlue, "Delivered")
        application.bouncedAt > 0 -> Triple(Icons.Default.Warning, ErrorRed, "Bounced")
        else -> Triple(Icons.Default.Send, TextGray, "Sent")
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailSheet(
    application: Application,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(application.jobTitle, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(application.companyName, color = PrimaryGreen, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            Text("Email Status", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            TimelineRow(Icons.Default.Send, "Application Sent", Formatters.formatDateTime(application.appliedAt), TextGray, true)
            TimelineRow(Icons.Default.CheckCircle, "Email Delivered", if (application.deliveredAt > 0) Formatters.formatDateTime(application.deliveredAt) else "Pending", InfoBlue, application.deliveredAt > 0)
            TimelineRow(Icons.Default.Visibility, "Opened by HR", if (application.openedAt > 0) Formatters.formatDateTime(application.openedAt) else "Pending", WarningOrange, application.openedAt > 0)
            TimelineRow(Icons.Default.Link, "Resume Viewed", if (application.clickedAt > 0) Formatters.formatDateTime(application.clickedAt) else "Pending", SuccessGreen, application.clickedAt > 0)

            Spacer(modifier = Modifier.height(24.dp))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isCompleted) color.copy(alpha = 0.2f) else SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isCompleted) color else TextGray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(title, color = if (isCompleted) TextWhite else TextGray.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal)
            Text(time, color = if (isCompleted) color else TextGray.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}