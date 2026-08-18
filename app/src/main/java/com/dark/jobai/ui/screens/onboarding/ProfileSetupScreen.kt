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
import com.dark.jobai.service.PdfExtractorService
import com.dark.jobai.service.StorageService
import com.dark.jobai.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfExtractor = remember { PdfExtractorService() }
    val storageService = remember { StorageService() }

    // Form state - Auto-filled from resume
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("") }
    var workPreference by remember { mutableStateOf("Remote") }

    // Job preferences
    var jobType by remember { mutableStateOf("All") }  // All, Remote, On-site, Hybrid
    var preferredLocations by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var isResumeUploaded by remember { mutableStateOf(false) }
    var extractedSummary by remember { mutableStateOf("") }

    // Resume picker
    val resumePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                try {
                    // Extract info from PDF
                    val resumeInfo = pdfExtractor.extractAllInfo(context, uri)

                    // Auto-fill form
                    if (fullName.isBlank()) fullName = resumeInfo.name
                    if (email.isBlank()) email = resumeInfo.email
                    if (phone.isBlank()) phone = resumeInfo.phone
                    if (location.isBlank()) location = resumeInfo.location
                    if (skillsText.isBlank()) skillsText = resumeInfo.skills.joinToString(", ")
                    if (experienceYears.isBlank()) experienceYears = resumeInfo.experienceYears

                    // Upload resume
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val result = storageService.uploadResume(userId, uri)
                        result.onSuccess { url ->
                            isResumeUploaded = true
                            extractedSummary = "✅ Extracted: ${resumeInfo.skills.size} skills, ${resumeInfo.name}"
                        }
                    }

                    isProcessing = false
                    onShowMessage("✅ Resume processed! Details auto-filled.")
                } catch (e: Exception) {
                    isProcessing = false
                    onShowMessage("❌ ${e.localizedMessage}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Complete Your Profile",
            color = TextWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Upload resume and we'll auto-fill everything!",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Resume Upload Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { resumePicker.launch("application/pdf") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(2.dp, if (isResumeUploaded) PrimaryGreen else PrimaryGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(48.dp))
                } else {
                    Icon(
                        if (isResumeUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    when {
                        isProcessing -> "Processing Resume..."
                        isResumeUploaded -> "Resume Uploaded ✓"
                        else -> "Upload Your Resume"
                    },
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (isResumeUploaded) extractedSummary else "PDF format • Auto-extract skills & details",
                    color = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Basic Info (Auto-filled)
        Text("Basic Information", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        ProfileField(fullName, { fullName = it }, "Full Name", Icons.Default.Person)
        ProfileField(headline, { headline = it }, "Headline", Icons.Default.Badge)
        ProfileField(email, { email = it }, "Email", Icons.Default.Email)
        ProfileField(phone, { phone = it }, "Phone", Icons.Default.Phone)
        ProfileField(location, { location = it }, "Location", Icons.Default.LocationOn)

        Spacer(modifier = Modifier.height(24.dp))

        // Skills (Auto-filled)
        Text("Skills", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        ProfileField(skillsText, { skillsText = it }, "Skills (comma separated)", Icons.Default.Star)
        ProfileField(experienceYears, { experienceYears = it }, "Years of Experience", Icons.Default.Timeline)
        ProfileField(preferredRole, { preferredRole = it }, "Preferred Role", Icons.Default.Work)

        Spacer(modifier = Modifier.height(24.dp))

        // Job Preferences
        Text("Job Preferences", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Job Type
        Text("Job Type", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Remote", "On-site", "Hybrid").forEach { type ->
                FilterChip(
                    selected = jobType == type,
                    onClick = { jobType = type },
                    label = { Text(type, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (jobType == type) PrimaryGreen else SurfaceDark,
                        labelColor = if (jobType == type) Color.Black else TextGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Work Preference
        Text("Work Preference", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Remote", "Hybrid", "On-site").forEach { type ->
                FilterChip(
                    selected = workPreference == type,
                    onClick = { workPreference = type },
                    label = { Text(type, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (workPreference == type) PrimaryGreen else SurfaceDark,
                        labelColor = if (workPreference == type) Color.Black else TextGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileField(preferredLocations, { preferredLocations = it }, "Preferred Locations (comma separated)", Icons.Default.LocationOn)
        ProfileField(salaryRange, { salaryRange = it }, "Expected Salary Range (Optional)", Icons.Default.Payments)

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                if (fullName.isBlank()) {
                    onShowMessage("Please enter your name")
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
                    "skills" to skills,
                    "experienceYears" to experienceYears,
                    "preferredRole" to preferredRole,
                    "workPreference" to workPreference,
                    "jobType" to jobType,
                    "preferredLocations" to locations,
                    "salaryRange" to salaryRange,
                    "profileCompleted" to true,
                    "updatedAt" to System.currentTimeMillis()
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        onShowMessage("✅ Profile saved!")
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
                containerColor = PrimaryGreen,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Save Profile & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ====================================================================
// PROFILE FIELD COMPONENT
// ====================================================================

@Composable
fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}