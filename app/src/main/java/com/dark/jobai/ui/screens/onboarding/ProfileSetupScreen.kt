package com.dark.jobai.ui.screens.onboarding

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.service.PdfExtractorService
import com.dark.jobai.service.StorageService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfExtractor = remember { PdfExtractorService() }
    val storageService = remember { StorageService() }

    // --- Form States ---
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var skillsText by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("") }
    var currentCompany by remember { mutableStateOf("") }
    var highestDegree by remember { mutableStateOf("") }

    var currentCtc by remember { mutableStateOf("") }
    var expectedCtc by remember { mutableStateOf("") }
    var noticePeriod by remember { mutableStateOf("Immediate") }

    var linkedinUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }

    var workPreference by remember { mutableStateOf("Remote") }
    var preferredLocations by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var isResumeUploaded by remember { mutableStateOf(false) }
    var extractedSummary by remember { mutableStateOf("") }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val SuccessGreen = Color(0xFF10B981)

    // Resume picker & extractor
    val resumePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                try {
                    val resumeInfo = pdfExtractor.extractAllInfo(context, uri)

                    if (fullName.isBlank()) fullName = resumeInfo.name
                    if (email.isBlank()) email = resumeInfo.email
                    if (phone.isBlank()) phone = resumeInfo.phone
                    if (location.isBlank()) location = resumeInfo.location
                    if (skillsText.isBlank()) skillsText = resumeInfo.skills.joinToString(", ")
                    if (experienceYears.isBlank()) experienceYears = resumeInfo.experienceYears

                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val result = storageService.uploadResume(userId, uri)
                        result.onSuccess {
                            isResumeUploaded = true
                            extractedSummary = "✅ Extracted: ${resumeInfo.skills.size} skills found & mapped!"
                        }
                    }

                    isProcessing = false
                    onShowMessage("✅ Resume processed successfully!")
                } catch (e: Exception) {
                    isProcessing = false
                    onShowMessage("❌ ${e.localizedMessage}")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Master Profile Setup",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "AI-Powered ATS Auto-Fill",
                        color = AppBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resume Upload Hero Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = AppBlue.copy(alpha = 0.08f))
                    .clickable { resumePicker.launch("application/pdf") },
                shape = RoundedCornerShape(22.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.5.dp, if (isResumeUploaded) SuccessGreen else AppBlue.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                if (isResumeUploaded)
                                    listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
                                else
                                    listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = AppBlue, modifier = Modifier.size(44.dp), strokeWidth = 3.dp)
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = (if (isResumeUploaded) SuccessGreen else AppBlue).copy(alpha = 0.15f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isResumeUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = if (isResumeUploaded) SuccessGreen else AppBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            when {
                                isProcessing -> "AI is parsing your resume..."
                                isResumeUploaded -> "Resume Linked & Parsed ✓"
                                else -> "Upload Master Resume (PDF)"
                            },
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            if (isResumeUploaded) extractedSummary else "Tap to auto-extract skills, experience & contact info",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ============ SECTION 1: PERSONAL INFO ============
            FormSectionCard(title = "1. Personal Information", icon = Icons.Default.Person) {
                ProfileField(fullName, { fullName = it }, "Full Name", Icons.Default.Badge)
                ProfileField(headline, { headline = it }, "Professional Headline (e.g. Senior Software Engineer)", Icons.Default.Work)
                ProfileField(email, { email = it }, "Email Address", Icons.Default.Email)
                ProfileField(phone, { phone = it }, "Phone Number", Icons.Default.Phone)
                ProfileField(location, { location = it }, "Current Location (City, Country)", Icons.Default.LocationOn)
                ProfileMultilineField(bio, { bio = it }, "Professional Summary / Bio", Icons.Default.Description)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ============ SECTION 2: PROFESSIONAL & SKILLS ============
            FormSectionCard(title = "2. Professional Details & Skills", icon = Icons.Default.CardTravel) {
                ProfileField(skillsText, { skillsText = it }, "Skills (comma separated)", Icons.Default.Star)
                ProfileField(experienceYears, { experienceYears = it }, "Years of Experience (e.g. 4)", Icons.Default.Timeline)
                ProfileField(preferredRole, { preferredRole = it }, "Target Role / Job Title", Icons.Default.TrackChanges)
                ProfileField(currentCompany, { currentCompany = it }, "Current / Last Company (Optional)", Icons.Default.Business)
                ProfileField(highestDegree, { highestDegree = it }, "Highest Education (e.g. B.Tech in CSE)", Icons.Default.School)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ============ SECTION 3: COMPENSATION & NOTICE PERIOD ============
            FormSectionCard(title = "3. Compensation & Availability", icon = Icons.Default.Payments) {
                ProfileField(currentCtc, { currentCtc = it }, "Current CTC (e.g. ₹12 LPA)", Icons.Default.AccountBalanceWallet)
                ProfileField(expectedCtc, { expectedCtc = it }, "Expected CTC (e.g. ₹18 LPA)", Icons.Default.Savings)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Notice Period", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Immediate", "15 Days", "1 Month", "2+ Months").forEach { period ->
                        val isSelected = noticePeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { noticePeriod = period },
                            label = { Text(period, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) AppBlue else Color(0xFFF1F5F9),
                                labelColor = if (isSelected) Color.White else TextMuted,
                                selectedContainerColor = AppBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BorderSubtle,
                                selectedBorderColor = AppBlue
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ============ SECTION 4: ONLINE PRESENCE ============
            FormSectionCard(title = "4. Online Presence", icon = Icons.Default.Public) {
                ProfileField(linkedinUrl, { linkedinUrl = it }, "LinkedIn Profile URL", Icons.Default.Link)
                ProfileField(githubUrl, { githubUrl = it }, "GitHub / GitLab Profile URL", Icons.Default.Code)
                ProfileField(portfolioUrl, { portfolioUrl = it }, "Portfolio / Personal Website URL", Icons.Default.Language)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ============ SECTION 5: JOB PREFERENCES ============
            FormSectionCard(title = "5. Job Preferences", icon = Modifier.let { Icons.Default.Tune }) {
                Text("Work Preference", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Remote", "Hybrid", "On-site").forEach { type ->
                        val isSelected = workPreference == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { workPreference = type },
                            label = { Text(type, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) AppBlue else Color(0xFFF1F5F9),
                                labelColor = if (isSelected) Color.White else TextMuted,
                                selectedContainerColor = AppBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BorderSubtle,
                                selectedBorderColor = AppBlue
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                ProfileField(preferredLocations, { preferredLocations = it }, "Preferred Job Locations (comma separated)", Icons.Default.LocationOn)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save & Continue Button
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        onShowMessage("Please enter your full name")
                        return@Button
                    }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    val skills = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val locations = preferredLocations.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                    val profileData = mapOf(
                        "fullName" to fullName,
                        "headline" to headline,
                        "email" to email,
                        "phone" to phone,
                        "location" to location,
                        "bio" to bio,
                        "skills" to skills,
                        "experienceYears" to experienceYears,
                        "preferredRole" to preferredRole,
                        "currentCompany" to currentCompany,
                        "highestDegree" to highestDegree,
                        "currentCtc" to currentCtc,
                        "expectedCtc" to expectedCtc,
                        "noticePeriod" to noticePeriod,
                        "linkedinUrl" to linkedinUrl,
                        "githubUrl" to githubUrl,
                        "portfolioUrl" to portfolioUrl,
                        "workPreference" to workPreference,
                        "preferredLocations" to locations,
                        "isProfileCompleted" to true,
                        "profileCompleted" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            onShowMessage("Master profile saved successfully!")
                            onProfileComplete()
                        }
                        .addOnFailureListener { e ->
                            onShowMessage("❌ ${e.localizedMessage}")
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Save Master Profile & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// ====================================================================
// DESIGN HELPERS (Clean Card & Field Components)
// ====================================================================

@Composable
fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    val AppBlue = Color(0xFF0F52FF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter $label", color = TextMuted.copy(alpha = 0.5f), fontSize = 13.sp) },
            leadingIcon = { Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = BorderSubtle,
                cursorColor = AppBlue,
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC)
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    val AppBlue = Color(0xFF0F52FF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            placeholder = { Text("Write a brief summary about your professional journey...", color = TextMuted.copy(alpha = 0.5f), fontSize = 13.sp) },
            leadingIcon = { Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = BorderSubtle,
                cursorColor = AppBlue,
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC)
            )
        )
    }
}