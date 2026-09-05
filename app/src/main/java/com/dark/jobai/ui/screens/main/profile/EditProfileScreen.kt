package com.dark.jobai.ui.screens.main.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.service.StorageService
import com.dark.jobai.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val storageService = remember { StorageService() }
    val scope = rememberCoroutineScope()

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
    var profileImageUrl by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    // Image Picker for Profile Avatar
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                isUploadingImage = true
                scope.launch {
                    val result = storageService.uploadProfileImage(userId, uri)
                    result.onSuccess { url ->
                        profileImageUrl = url
                        isUploadingImage = false
                        Toast.makeText(context, "✅ Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        isUploadingImage = false
                        Toast.makeText(context, "❌ Failed to upload image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

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
            profileImageUrl = it.profileImageUrl
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgLight,
                    titleContentColor = TextDark
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
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
                                profileImageUrl = profileImageUrl,
                                profileCompleted = true,
                                updatedAt = System.currentTimeMillis()
                            )

                            if (updatedUser != null) {
                                profileViewModel.saveProfile(updatedUser)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Profile Picture Upload Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    if (isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to change profile picture",
                    color = AppBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { imagePicker.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 1: Personal Info
            EditSection(title = "Personal Information", icon = Icons.Default.Person) {
                EditTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    icon = Icons.Default.Badge
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = "Headline",
                    icon = Icons.Default.Work
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    icon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    icon = Icons.Default.LocationOn
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Professional Details
            EditSection(title = "Professional Details", icon = Icons.Default.Description) {
                EditMultiLineTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "About Me (Bio)",
                    icon = Icons.Default.HistoryEdu
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = "Skills (comma separated)",
                    icon = Icons.Default.Star
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Job Preferences
            EditSection(title = "Job Preferences", icon = Icons.Default.AdsClick) {
                EditTextField(
                    value = preferredRole,
                    onValueChange = { preferredRole = it },
                    label = "Target Role",
                    icon = Icons.Default.TrackChanges
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = "Years of Experience",
                    icon = Icons.Default.Timeline
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Work Preference",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Remote", "Hybrid", "On-site").forEach { type ->
                        val isSelected = workPreference == type
                        Surface(
                            onClick = { workPreference = type },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AppBlue else SurfaceWhite,
                            border = BorderStroke(1.dp, if (isSelected) AppBlue else BorderSubtle),
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Social Presence
            EditSection(title = "Online Presence", icon = Icons.Default.Public) {
                EditTextField(
                    value = linkedinUrl,
                    onValueChange = { linkedinUrl = it },
                    label = "LinkedIn Profile",
                    icon = Icons.Default.Link
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = githubUrl,
                    onValueChange = { githubUrl = it },
                    label = "GitHub Profile",
                    icon = Icons.Default.Code
                )
                Spacer(modifier = Modifier.height(14.dp))
                EditTextField(
                    value = portfolioUrl,
                    onValueChange = { portfolioUrl = it },
                    label = "Portfolio/Website",
                    icon = Icons.Default.Language
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = BorderSubtle,
                cursorColor = AppBlue,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMultiLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = BorderSubtle,
                cursorColor = AppBlue,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            )
        )
    }
}