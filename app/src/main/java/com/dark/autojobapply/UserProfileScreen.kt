package com.dark.autojobapply

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.ui.theme.BackgroundDark
import com.dark.jobai.ui.theme.BorderGray
import com.dark.jobai.ui.theme.ErrorRed
import com.dark.jobai.ui.theme.PrimaryGreen
import com.dark.jobai.ui.theme.TextGray
import com.dark.jobai.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// ====================================================================
// USER PROFILE SCREEN - View & Edit
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()

    // Profile data
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }
    var experienceYears by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("") }
    var workPreference by remember { mutableStateOf("Remote") }
    var resumeUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var twitterUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var currentSalary by remember { mutableStateOf("") }
    var expectedSalary by remember { mutableStateOf("") }
    var noticePeriod by remember { mutableStateOf("") }
    var totalApplications by remember { mutableStateOf(0) }
    var isPremium by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showResumeSheet by remember { mutableStateOf(false) }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploadProfileImage(storage, auth.currentUser?.uid, uri) { url ->
                    profileImageUrl = url
                    saveToFirestore(db, auth.currentUser?.uid, "profileImageUrl", url)
                }
            }
        }
    }

    // Resume picker
    val resumePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploadResume(storage, auth.currentUser?.uid, uri) { url ->
                    resumeUrl = url
                    saveToFirestore(db, auth.currentUser?.uid, "resumeUrl", url)
                }
            }
        }
    }

    // Load profile data
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    fullName = doc.getString("fullName") ?: ""
                    headline = doc.getString("headline") ?: ""
                    email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                    phone = doc.getString("phone") ?: ""
                    location = doc.getString("location") ?: ""
                    skills = (doc.get("skills") as? List<String>) ?: emptyList()
                    experienceYears = doc.getString("experienceYears") ?: ""
                    preferredRole = doc.getString("preferredRole") ?: ""
                    workPreference = doc.getString("workPreference") ?: "Remote"
                    resumeUrl = doc.getString("resumeUrl") ?: ""
                    linkedinUrl = doc.getString("linkedinUrl") ?: ""
                    portfolioUrl = doc.getString("portfolioUrl") ?: ""
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    githubUrl = doc.getString("githubUrl") ?: ""
                    twitterUrl = doc.getString("twitterUrl") ?: ""
                    bio = doc.getString("bio") ?: ""
                    currentSalary = doc.getString("currentSalary") ?: ""
                    expectedSalary = doc.getString("expectedSalary") ?: ""
                    noticePeriod = doc.getString("noticePeriod") ?: ""
                    isPremium = doc.getBoolean("isPremium") ?: false
                }

                // Get application count
                val appsSnapshot = db.collection("users")
                    .document(userId)
                    .collection("applications")
                    .get()
                    .await()
                totalApplications = appsSnapshot.size()

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    // Save profile
    fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return

        isSaving = true

        val profileData = hashMapOf(
            "fullName" to fullName,
            "headline" to headline,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "skills" to skills,
            "experienceYears" to experienceYears,
            "preferredRole" to preferredRole,
            "workPreference" to workPreference,
            "resumeUrl" to resumeUrl,
            "linkedinUrl" to linkedinUrl,
            "portfolioUrl" to portfolioUrl,
            "profileImageUrl" to profileImageUrl,
            "githubUrl" to githubUrl,
            "twitterUrl" to twitterUrl,
            "bio" to bio,
            "currentSalary" to currentSalary,
            "expectedSalary" to expectedSalary,
            "noticePeriod" to noticePeriod,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isSaving = false
                isEditMode = false
                Toast.makeText(context, "✅ Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isSaving = false
                Toast.makeText(context, "❌ Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    Text(
                        "My Profile",
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Edit/Save Button
                if (isLoading) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                } else {
                    TextButton(
                        onClick = {
                            if (isEditMode) {
                                saveProfile()
                            } else {
                                isEditMode = true
                            }
                        }
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isEditMode) "Save" else "Edit",
                            color = PrimaryGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Profile Header
                    ProfileHeader(
                        fullName = fullName,
                        headline = headline,
                        location = location,
                        profileImageUrl = profileImageUrl,
                        isPremium = isPremium,
                        totalApplications = totalApplications,
                        onImageClick = {
                            if (isEditMode) {
                                imagePicker.launch("image/*")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    ProfileStats(
                        applications = totalApplications,
                        skillsCount = skills.size,
                        experienceYears = experienceYears
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isEditMode) {
                        // EDIT MODE
                        EditProfileForm(
                            fullName = fullName,
                            onFullNameChange = { fullName = it },
                            headline = headline,
                            onHeadlineChange = { headline = it },
                            email = email,
                            onEmailChange = { email = it },
                            phone = phone,
                            onPhoneChange = { phone = it },
                            location = location,
                            onLocationChange = { location = it },
                            bio = bio,
                            onBioChange = { bio = it },
                            skills = skills,
                            onSkillsChange = { skills = it },
                            experienceYears = experienceYears,
                            onExperienceChange = { experienceYears = it },
                            preferredRole = preferredRole,
                            onPreferredRoleChange = { preferredRole = it },
                            workPreference = workPreference,
                            onWorkPreferenceChange = { workPreference = it },
                            currentSalary = currentSalary,
                            onCurrentSalaryChange = { currentSalary = it },
                            expectedSalary = expectedSalary,
                            onExpectedSalaryChange = { expectedSalary = it },
                            noticePeriod = noticePeriod,
                            onNoticePeriodChange = { noticePeriod = it },
                            linkedinUrl = linkedinUrl,
                            onLinkedinChange = { linkedinUrl = it },
                            githubUrl = githubUrl,
                            onGithubChange = { githubUrl = it },
                            portfolioUrl = portfolioUrl,
                            onPortfolioChange = { portfolioUrl = it },
                            twitterUrl = twitterUrl,
                            onTwitterChange = { twitterUrl = it },
                            resumeUrl = resumeUrl,
                            onResumeClick = {
                                if (isEditMode) {
                                    resumePicker.launch("application/pdf")
                                }
                            }
                        )
                    } else {
                        // VIEW MODE
                        ViewProfileDetails(
                            fullName = fullName,
                            headline = headline,
                            bio = bio,
                            email = email,
                            phone = phone,
                            location = location,
                            skills = skills,
                            experienceYears = experienceYears,
                            preferredRole = preferredRole,
                            workPreference = workPreference,
                            currentSalary = currentSalary,
                            expectedSalary = expectedSalary,
                            noticePeriod = noticePeriod,
                            resumeUrl = resumeUrl,
                            linkedinUrl = linkedinUrl,
                            githubUrl = githubUrl,
                            portfolioUrl = portfolioUrl,
                            twitterUrl = twitterUrl,
                            onResumeClick = {
                                if (resumeUrl.isNotEmpty()) {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        Uri.parse(resumeUrl)
                                    )
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Logout Button
                    OutlinedButton(
                        onClick = {
                            auth.signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        border = BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ====================================================================
// PROFILE HEADER
// ====================================================================

@Composable
fun ProfileHeader(
    fullName: String,
    headline: String,
    location: String,
    profileImageUrl: String,
    isPremium: Boolean,
    totalApplications: Int,
    onImageClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isPremium)
                            listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                        else
                            listOf(PrimaryGreen, Color(0xFF00D2A0))
                    )
                )
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    fullName.take(1).uppercase(),
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Premium Badge
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                fullName.ifEmpty { "Your Name" },
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (headline.isNotEmpty()) {
                Text(
                    headline,
                    color = PrimaryGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (location.isNotEmpty()) {
                Text(
                    "📍 $location",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ====================================================================
// PROFILE STATS
// ====================================================================

@Composable
fun ProfileStats(
    applications: Int,
    skillsCount: Int,
    experienceYears: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                applications.toString(),
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Applications", color = TextGray, fontSize = 11.sp)
        }

        VerticalDivider(color = BorderGray, modifier = Modifier.height(30.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                skillsCount.toString(),
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Skills", color = TextGray, fontSize = 11.sp)
        }

        VerticalDivider(color = BorderGray, modifier = Modifier.height(30.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                experienceYears.ifEmpty { "0" },
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Years Exp", color = TextGray, fontSize = 11.sp)
        }
    }
}

// ====================================================================
// VIEW PROFILE DETAILS
// ====================================================================

@Composable
fun ViewProfileDetails(
    fullName: String,
    headline: String,
    bio: String,
    email: String,
    phone: String,
    location: String,
    skills: List<String>,
    experienceYears: String,
    preferredRole: String,
    workPreference: String,
    currentSalary: String,
    expectedSalary: String,
    noticePeriod: String,
    resumeUrl: String,
    linkedinUrl: String,
    githubUrl: String,
    portfolioUrl: String,
    twitterUrl: String,
    onResumeClick: () -> Unit
) {
    Column {
        // Bio
        if (bio.isNotEmpty()) {
            SectionCard(title = "About", icon = Icons.Default.Person) {
                Text(bio, color = TextWhite.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contact Info
        SectionCard(title = "Contact Information", icon = Icons.Default.Contacts) {
            InfoRow(Icons.Default.Email, "Email", email)
            if (phone.isNotEmpty()) InfoRow(Icons.Default.Phone, "Phone", phone)
            if (location.isNotEmpty()) InfoRow(Icons.Default.LocationOn, "Location", location)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skills
        SectionCard(title = "Skills", icon = Icons.Default.Star) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(skill, color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Job Preferences
        SectionCard(title = "Job Preferences", icon = Icons.Default.Work) {
            if (preferredRole.isNotEmpty()) InfoRow(Icons.Default.Badge, "Preferred Role", preferredRole)
            if (experienceYears.isNotEmpty()) InfoRow(Icons.Default.Timeline, "Experience", "$experienceYears years")
            InfoRow(Icons.Default.Business, "Work Preference", workPreference)
            if (currentSalary.isNotEmpty()) InfoRow(Icons.Default.Payments, "Current Salary", currentSalary)
            if (expectedSalary.isNotEmpty()) InfoRow(Icons.Default.TrendingUp, "Expected Salary", expectedSalary)
            if (noticePeriod.isNotEmpty()) InfoRow(Icons.Default.Schedule, "Notice Period", noticePeriod)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resume
        SectionCard(title = "Resume", icon = Icons.Default.Description) {
            if (resumeUrl.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onResumeClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Resume", color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text("No resume uploaded", color = TextGray, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Links
        SectionCard(title = "Social Links", icon = Icons.Default.Link) {
            if (linkedinUrl.isNotEmpty()) InfoRow(Icons.Default.Link, "LinkedIn", linkedinUrl)
            if (githubUrl.isNotEmpty()) InfoRow(Icons.Default.Code, "GitHub", githubUrl)
            if (portfolioUrl.isNotEmpty()) InfoRow(Icons.Default.Public, "Portfolio", portfolioUrl)
            if (twitterUrl.isNotEmpty()) InfoRow(Icons.Default.Share, "Twitter", twitterUrl)
        }
    }
}

// ====================================================================
// EDIT PROFILE FORM
// ====================================================================

@Composable
fun EditProfileForm(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    headline: String,
    onHeadlineChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    skills: List<String>,
    onSkillsChange: (List<String>) -> Unit,
    experienceYears: String,
    onExperienceChange: (String) -> Unit,
    preferredRole: String,
    onPreferredRoleChange: (String) -> Unit,
    workPreference: String,
    onWorkPreferenceChange: (String) -> Unit,
    currentSalary: String,
    onCurrentSalaryChange: (String) -> Unit,
    expectedSalary: String,
    onExpectedSalaryChange: (String) -> Unit,
    noticePeriod: String,
    onNoticePeriodChange: (String) -> Unit,
    linkedinUrl: String,
    onLinkedinChange: (String) -> Unit,
    githubUrl: String,
    onGithubChange: (String) -> Unit,
    portfolioUrl: String,
    onPortfolioChange: (String) -> Unit,
    twitterUrl: String,
    onTwitterChange: (String) -> Unit,
    resumeUrl: String,
    onResumeClick: () -> Unit
) {
    var skillsText by remember { mutableStateOf(skills.joinToString(", ")) }

    Column {
        // Basic Info
        EditSection(title = "Basic Information") {
            EditTextField(fullName, onFullNameChange, "Full Name", Icons.Default.Person)
            EditTextField(headline, onHeadlineChange, "Headline", Icons.Default.Badge)
            EditTextField(email, onEmailChange, "Email", Icons.Default.Email)
            EditTextField(phone, onPhoneChange, "Phone", Icons.Default.Phone)
            EditTextField(location, onLocationChange, "Location", Icons.Default.LocationOn)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bio
        EditSection(title = "About Me") {
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Tell us about yourself...") },
                shape = RoundedCornerShape(12.dp),
                colors = darkTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skills
        EditSection(title = "Skills (comma separated)") {
            OutlinedTextField(
                value = skillsText,
                onValueChange = {
                    skillsText = it
                    onSkillsChange(it.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() })
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Kotlin, Java, Firebase...") },
                shape = RoundedCornerShape(12.dp),
                colors = darkTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Job Preferences
        EditSection(title = "Job Preferences") {
            EditTextField(experienceYears, onExperienceChange, "Years of Experience", Icons.Default.Timeline)
            EditTextField(preferredRole, onPreferredRoleChange, "Preferred Role", Icons.Default.Work)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Work Preference", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Remote", "Hybrid", "On-site").forEach { type ->
                    FilterChip(
                        selected = workPreference == type,
                        onClick = { onWorkPreferenceChange(type) },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (workPreference == type) PrimaryGreen else Color(0xFF1A1A1A),
                            labelColor = if (workPreference == type) Color.Black else TextGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Salary
        EditSection(title = "Salary Details") {
            EditTextField(currentSalary, onCurrentSalaryChange, "Current Salary (Optional)", Icons.Default.Payments)
            EditTextField(expectedSalary, onExpectedSalaryChange, "Expected Salary (Optional)", Icons.Default.TrendingUp)
            EditTextField(noticePeriod, onNoticePeriodChange, "Notice Period (Optional)", Icons.Default.Schedule)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resume
        EditSection(title = "Resume") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onResumeClick),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, if (resumeUrl.isNotEmpty()) PrimaryGreen else BorderGray)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Upload,
                        null,
                        tint = if (resumeUrl.isNotEmpty()) PrimaryGreen else TextGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (resumeUrl.isNotEmpty()) "Resume Uploaded - Click to Change" else "Upload Resume (PDF)",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Links
        EditSection(title = "Social Links") {
            EditTextField(linkedinUrl, onLinkedinChange, "LinkedIn URL", Icons.Default.Link)
            EditTextField(githubUrl, onGithubChange, "GitHub URL", Icons.Default.Code)
            EditTextField(portfolioUrl, onPortfolioChange, "Portfolio URL", Icons.Default.Public)
            EditTextField(twitterUrl, onTwitterChange, "Twitter URL", Icons.Default.Share)
        }
    }
}

// ====================================================================
// REUSABLE COMPONENTS
// ====================================================================

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextGray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = TextGray, fontSize = 10.sp)
            Text(value, color = TextWhite, fontSize = 13.sp)
        }
    }
}

@Composable
fun EditSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    Column {
        Text(label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter $label") },
            leadingIcon = { Icon(icon, null, tint = TextGray) },
            shape = RoundedCornerShape(12.dp),
            colors = darkTextFieldColors(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun darkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryGreen,
    focusedContainerColor = Color(0xFF1A1A1A),
    unfocusedContainerColor = Color(0xFF1A1A1A)
)

// ====================================================================
// UPLOAD HELPERS
// ====================================================================

private suspend fun uploadProfileImage(
    storage: FirebaseStorage,
    userId: String?,
    uri: Uri,
    onComplete: (String) -> Unit
) {
    if (userId == null) return

    val fileRef = storage.reference.child("profileImages/$userId/${UUID.randomUUID()}.jpg")

    fileRef.putFile(uri)
        .await()

    val downloadUrl = fileRef.downloadUrl.await()
    onComplete(downloadUrl.toString())
}

private suspend fun uploadResume(
    storage: FirebaseStorage,
    userId: String?,
    uri: Uri,
    onComplete: (String) -> Unit
) {
    if (userId == null) return

    val fileRef = storage.reference.child("resumes/$userId/${UUID.randomUUID()}.pdf")

    fileRef.putFile(uri)
        .await()

    val downloadUrl = fileRef.downloadUrl.await()
    onComplete(downloadUrl.toString())
}

private fun saveToFirestore(
    db: FirebaseFirestore,
    userId: String?,
    field: String,
    value: Any
) {
    if (userId == null) return

    db.collection("users").document(userId)
        .update(field, value)
}