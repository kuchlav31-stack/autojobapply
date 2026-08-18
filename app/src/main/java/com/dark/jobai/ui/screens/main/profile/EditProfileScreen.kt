package com.dark.jobai.ui.screens.main.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.data.model.User
import com.dark.jobai.ui.components.AppTextField
import com.dark.jobai.ui.components.PrimaryButton
import com.dark.jobai.ui.theme.*
import com.dark.jobai.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val user by profileViewModel.user.collectAsState()
    val isSaving by profileViewModel.isSaving.collectAsState()
    val successMessage by profileViewModel.successMessage.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    // Form state
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("") }
    var workPreference by remember { mutableStateOf("Remote") }
    var linkedinUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }

    // Load user data
    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            headline = it.headline
            phone = it.phone
            location = it.location
            bio = it.bio
            skillsText = it.skills.joinToString(", ")
            experienceYears = it.experienceYears
            preferredRole = it.preferredRole
            workPreference = it.workPreference
            linkedinUrl = it.linkedinUrl
            githubUrl = it.githubUrl
            portfolioUrl = it.portfolioUrl
        }
    }

    // Show messages
    LaunchedEffect(successMessage, errorMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearMessages()
            onSaved()
        }
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearMessages()
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Edit Profile",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Basic Info
            Text("Basic Information", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                icon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = headline,
                onValueChange = { headline = it },
                label = "Headline",
                placeholder = "Senior Android Developer",
                icon = Icons.Default.Badge
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone",
                icon = Icons.Default.Phone
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                icon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Skills
            Text("Skills", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = skillsText,
                onValueChange = { skillsText = it },
                label = "Skills (comma separated)",
                placeholder = "Kotlin, Java, Firebase",
                icon = Icons.Default.Star
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Job Preferences
            Text("Job Preferences", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = "Years of Experience",
                icon = Icons.Default.Timeline
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = preferredRole,
                onValueChange = { preferredRole = it },
                label = "Preferred Role",
                icon = Icons.Default.Work
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Work Preference", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Remote", "Hybrid", "On-site").forEach { type ->
                    FilterChip(
                        selected = workPreference == type,
                        onClick = { workPreference = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (workPreference == type) PrimaryGreen else SurfaceDark,
                            labelColor = if (workPreference == type) Color.Black else TextGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Links
            Text("Social Links", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = linkedinUrl,
                onValueChange = { linkedinUrl = it },
                label = "LinkedIn URL",
                icon = Icons.Default.Link
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = "GitHub URL",
                icon = Icons.Default.Code
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = portfolioUrl,
                onValueChange = { portfolioUrl = it },
                label = "Portfolio URL",
                icon = Icons.Default.Public
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            PrimaryButton(
                text = "Save Changes",
                onClick = {
                    val updatedUser = user?.copy(
                        fullName = fullName,
                        headline = headline,
                        phone = phone,
                        location = location,
                        bio = bio,
                        skills = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        experienceYears = experienceYears,
                        preferredRole = preferredRole,
                        workPreference = workPreference,
                        linkedinUrl = linkedinUrl,
                        githubUrl = githubUrl,
                        portfolioUrl = portfolioUrl,
                        profileCompleted = true,
                        updatedAt = System.currentTimeMillis()
                    )

                    if (updatedUser != null) {
                        profileViewModel.saveProfile(updatedUser)
                    }
                },
                isLoading = isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}