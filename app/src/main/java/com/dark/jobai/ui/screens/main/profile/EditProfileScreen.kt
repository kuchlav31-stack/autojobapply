package com.dark.jobai.ui.screens.main.profile

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.service.StorageService
import com.dark.jobai.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ====================================================================
// APPLYAI OFFICIAL COLOR PALETTE
// ====================================================================
private val ApplyAiBlue = Color(0xFF0066FF)
private val ApplyAiBlueLight = Color(0xFFEBF3FF)
private val ApplyAiCanvas = Color(0xFFF4F8FF)
private val ApplyAiCard = Color(0xFFFFFFFF)
private val ApplyAiBorder = Color(0xFFE5EDF9)
private val ApplyAiTextDark = Color(0xFF19213D)
private val ApplyAiTextSub = Color(0xFF6B7897)
private val ApplyAiTextMuted = Color(0xFF9BA6C1)
private val ApplyAiGreen = Color(0xFF00C781)
private val ApplyAiCoral = Color(0xFFFF5274)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // --- FORM STATES (All User model fields) ---
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }

    // Master ATS Fields
    var preferredRole by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var currentCompany by remember { mutableStateOf("") }
    var highestDegree by remember { mutableStateOf("") }
    var currentCtc by remember { mutableStateOf("") }
    var expectedCtc by remember { mutableStateOf("") }
    var noticePeriod by remember { mutableStateOf("") }

    // Preferences
    var workPreference by remember { mutableStateOf("Remote") }
    var jobType by remember { mutableStateOf("Full-time") }

    // Web & Social
    var linkedinUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }
    var twitterUrl by remember { mutableStateOf("") }

    // Media Uploads
    var profileImageUrl by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }

    var resumeUrl by remember { mutableStateOf("") }
    var resumeFileName by remember { mutableStateOf("") }
    var isUploadingResume by remember { mutableStateOf(false) }

    // 1. Image Picker (Profile Avatar)
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
                        Toast.makeText(context, "✅ Profile picture uploaded!", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        isUploadingImage = false
                        Toast.makeText(context, "❌ Upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 2. Document Picker (Resume PDF / DOCX)
    val resumePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Extract file name
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex != -1) {
                            resumeFileName = cursor.getString(nameIndex)
                        }
                    }
                } catch (e: Exception) {}

                isUploadingResume = true
                scope.launch {
                    try {
                        val result = storageService.uploadResume(userId, uri)
                        result.onSuccess { url ->
                            resumeUrl = url
                            isUploadingResume = false
                            Toast.makeText(context, "✅ Resume uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }.onFailure { e ->
                            isUploadingResume = false
                            Toast.makeText(context, "❌ Resume upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        isUploadingResume = false
                        Toast.makeText(context, "❌ Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Load initial user data
    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            headline = it.headline
            phone = it.phone
            location = it.location
            bio = it.bio
            skillsText = it.skills.joinToString(", ")
            preferredRole = it.preferredRole
            experienceYears = it.experienceYears
            currentCompany = it.currentCompany
            highestDegree = it.highestDegree
            currentCtc = it.currentCtc
            expectedCtc = it.expectedCtc
            noticePeriod = it.noticePeriod
            workPreference = it.workPreference.ifEmpty { "Remote" }
            jobType = it.jobType.ifEmpty { "Full-time" }
            linkedinUrl = it.linkedinUrl
            githubUrl = it.githubUrl
            portfolioUrl = it.portfolioUrl
            twitterUrl = it.twitterUrl
            profileImageUrl = it.profileImageUrl
            resumeUrl = it.resumeUrl
            if (it.resumeUrl.isNotEmpty()) {
                resumeFileName = "Uploaded_Resume.pdf"
            }
        }
    }

    // Success / Error listener
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
        containerColor = ApplyAiCanvas,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Master Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = ApplyAiTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ApplyAiTextDark, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ApplyAiCanvas,
                    titleContentColor = ApplyAiTextDark
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ApplyAiCard,
                border = BorderStroke(1.dp, ApplyAiBorder),
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            val updatedSkills = skillsText
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            val updatedUser = user?.copy(
                                fullName = fullName,
                                headline = headline,
                                phone = phone,
                                location = location,
                                bio = bio,
                                skills = updatedSkills,
                                preferredRole = preferredRole,
                                experienceYears = experienceYears,
                                currentCompany = currentCompany,
                                highestDegree = highestDegree,
                                currentCtc = currentCtc,
                                expectedCtc = expectedCtc,
                                noticePeriod = noticePeriod,
                                workPreference = workPreference,
                                jobType = jobType,
                                linkedinUrl = linkedinUrl,
                                githubUrl = githubUrl,
                                portfolioUrl = portfolioUrl,
                                twitterUrl = twitterUrl,
                                profileImageUrl = profileImageUrl,
                                resumeUrl = resumeUrl,
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApplyAiBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ====================================================================
            // 1. AVATAR UPLOAD WITH APPLYAI HALO
            // ====================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(ApplyAiBlueLight)
                        .border(2.5.dp, ApplyAiBlue, CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ApplyAiBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    if (isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isUploadingImage) "Uploading..." else "Tap to change photo",
                    color = ApplyAiBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { imagePicker.launch("image/*") }
                )
            }

            // ====================================================================
            // 2. RESUME UPLOAD SECTION (Exact Screen 9 "Add Your Resume" style)
            // ====================================================================
            ApplyAiEditCard(title = "Resume / CV Document", icon = Icons.Default.UploadFile) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApplyAiCanvas)
                        .border(1.5.dp, ApplyAiBorder, RoundedCornerShape(16.dp))
                        .clickable { resumePicker.launch("application/pdf") }
                        .padding(16.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFECEF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = ApplyAiCoral, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (resumeUrl.isNotEmpty()) resumeFileName.ifEmpty { "Master_Resume.pdf" } else "Upload Resume (PDF, DOC)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApplyAiTextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (resumeUrl.isNotEmpty()) "Uploaded • Tap to replace" else "Max 5MB • Used by AI to apply",
                                fontSize = 11.sp,
                                color = if (resumeUrl.isNotEmpty()) ApplyAiGreen else ApplyAiTextSub
                            )
                        }

                        if (isUploadingResume) {
                            CircularProgressIndicator(color = ApplyAiBlue, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (resumeUrl.isNotEmpty()) {
                            Icon(Icons.Default.CheckCircle, null, tint = ApplyAiGreen, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.CloudUpload, null, tint = ApplyAiBlue, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // ====================================================================
            // 3. PERSONAL & CONTACT DETAILS
            // ====================================================================
            ApplyAiEditCard(title = "Personal Information", icon = Icons.Default.PersonOutline) {
                ApplyAiTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Badge)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = headline, onValueChange = { headline = it }, label = "Headline (e.g. Senior Android Dev)", icon = Icons.Default.WorkOutline)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = location, onValueChange = { location = it }, label = "Location (City, Country)", icon = Icons.Default.Place)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiMultiLineField(value = bio, onValueChange = { bio = it }, label = "Professional Summary / Bio")
            }

            // ====================================================================
            // 4. MASTER ATS CANDIDATE PARAMETERS (NEW MASTER PROFILE FIELDS)
            // ====================================================================
            ApplyAiEditCard(title = "Master ATS Parameters", icon = Icons.Default.FactCheck) {
                ApplyAiTextField(value = preferredRole, onValueChange = { preferredRole = it }, label = "Target Job Role", icon = Icons.Default.AdsClick)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = experienceYears, onValueChange = { experienceYears = it }, label = "Total Experience (in Years)", icon = Icons.Default.Timeline)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = currentCompany, onValueChange = { currentCompany = it }, label = "Current / Last Company", icon = Icons.Default.Business)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = highestDegree, onValueChange = { highestDegree = it }, label = "Highest Education / Degree", icon = Icons.Default.School)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = currentCtc, onValueChange = { currentCtc = it }, label = "Current CTC (e.g. ₹12 LPA or \$80k)", icon = Icons.Default.CurrencyRupee)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = expectedCtc, onValueChange = { expectedCtc = it }, label = "Expected CTC (e.g. ₹18 LPA or \$110k)", icon = Icons.Default.TrendingUp)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = noticePeriod, onValueChange = { noticePeriod = it }, label = "Notice Period (e.g. Immediate / 30 Days)", icon = Icons.Default.HourglassTop)
            }

            // ====================================================================
            // 5. JOB & WORK PREFERENCE CAPSULES (Screen 8 Style)
            // ====================================================================
            ApplyAiEditCard(title = "Job Matching Preferences", icon = Icons.Default.Tune) {
                // Work Preference
                Text("Work Mode Preference", color = ApplyAiTextSub, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Remote", "Hybrid", "On-site").forEach { type ->
                        val isSelected = workPreference.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) ApplyAiBlue else ApplyAiCanvas)
                                .border(1.dp, if (isSelected) ApplyAiBlue else ApplyAiBorder, RoundedCornerShape(50))
                                .clickable { workPreference = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) Color.White else ApplyAiTextDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Job Type
                Text("Job Type", color = ApplyAiTextSub, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Full-time", "Part-time", "Internship").forEach { type ->
                        val isSelected = jobType.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) ApplyAiBlue else ApplyAiCanvas)
                                .border(1.dp, if (isSelected) ApplyAiBlue else ApplyAiBorder, RoundedCornerShape(50))
                                .clickable { jobType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) Color.White else ApplyAiTextDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ====================================================================
            // 6. SKILLS (Live Capsule Preview)
            // ====================================================================
            ApplyAiEditCard(title = "Skills & Expertise", icon = Icons.Default.Bolt) {
                ApplyAiTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = "Add skills (comma separated)",
                    icon = Icons.Default.Code
                )

                val previewSkills = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (previewSkills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        previewSkills.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ApplyAiBlueLight)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(skill, color = ApplyAiBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ====================================================================
            // 7. ONLINE & PORTFOLIO LINKS
            // ====================================================================
            ApplyAiEditCard(title = "Web & Social Presence", icon = Icons.Default.Language) {
                ApplyAiTextField(value = linkedinUrl, onValueChange = { linkedinUrl = it }, label = "LinkedIn Profile", icon = Icons.Default.Link)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = githubUrl, onValueChange = { githubUrl = it }, label = "GitHub Profile", icon = Icons.Default.Code)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = portfolioUrl, onValueChange = { portfolioUrl = it }, label = "Personal Portfolio", icon = Icons.Default.Language)
                Spacer(modifier = Modifier.height(12.dp))
                ApplyAiTextField(value = twitterUrl, onValueChange = { twitterUrl = it }, label = "Twitter / X Profile", icon = Icons.Default.AlternateEmail)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ====================================================================
// REUSABLE EDIT COMPONENTS (APPLYAI DESIGN SYSTEM)
// ====================================================================

@Composable
fun ApplyAiEditCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = ApplyAiBlue.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(20.dp),
        color = ApplyAiCard,
        border = BorderStroke(1.dp, ApplyAiBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApplyAiBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = ApplyAiBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = ApplyAiTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun ApplyAiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    Column {
        Text(label, color = ApplyAiTextSub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(icon, null, tint = ApplyAiTextMuted, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ApplyAiTextDark,
                unfocusedTextColor = ApplyAiTextDark,
                focusedBorderColor = ApplyAiBlue,
                unfocusedBorderColor = ApplyAiBorder,
                focusedContainerColor = ApplyAiCanvas,
                unfocusedContainerColor = ApplyAiCanvas,
                cursorColor = ApplyAiBlue
            ),
            singleLine = true
        )
    }
}

@Composable
fun ApplyAiMultiLineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column {
        Text(label, color = ApplyAiTextSub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ApplyAiTextDark,
                unfocusedTextColor = ApplyAiTextDark,
                focusedBorderColor = ApplyAiBlue,
                unfocusedBorderColor = ApplyAiBorder,
                focusedContainerColor = ApplyAiCanvas,
                unfocusedContainerColor = ApplyAiCanvas,
                cursorColor = ApplyAiBlue
            )
        )
    }
}