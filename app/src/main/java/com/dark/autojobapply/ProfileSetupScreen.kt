package com.dark.autojobapply

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.autojobapply.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()

    // Form state
    var fullName by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("") }
    var workPreference by remember { mutableStateOf("Remote") }
    var resumeUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }

    // Resume state
    var extractedText by remember { mutableStateOf("") }
    var extractedSkills by remember { mutableStateOf<List<String>>(emptyList()) }
    var extractedName by remember { mutableStateOf("") }
    var extractedEmail by remember { mutableStateOf("") }
    var extractedPhone by remember { mutableStateOf("") }
    var extractedLocation by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    var resumeUploaded by remember { mutableStateOf(false) }
    var resumeProcessed by remember { mutableStateOf(false) }

    var viewAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { viewAlpha = 1f }
    val entranceAlpha by animateFloatAsState(
        targetValue = viewAlpha,
        animationSpec = tween(800)
    )

    // Resume picker - AUTO-FILL from resume
    val resumePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            scope.launch {
                try {
                    // Extract text from PDF
                    val text = withContext(Dispatchers.IO) {
                        extractTextFromPdf(context, uri)
                    }

                    if (text.isNotBlank()) {
                        extractedText = text

                        // Extract all info from resume
                        extractedSkills = extractSkillsFromResume(text)
                        extractedName = extractNameFromResume(text)
                        extractedEmail = extractEmailFromResume(text) ?: email
                        extractedPhone = extractPhoneFromResume(text)
                        extractedLocation = extractLocationFromResume(text)

                        // AUTO-FILL form fields if empty
                        if (fullName.isBlank()) fullName = extractedName
                        if (email.isBlank()) email = extractedEmail
                        if (phone.isBlank()) phone = extractedPhone
                        if (location.isBlank()) location = extractedLocation
                        if (skills.isBlank() && extractedSkills.isNotEmpty()) {
                            skills = extractedSkills.joinToString(", ")
                        }

                        // Upload resume to Firebase Storage
                        uploadResume(
                            storage = storage,
                            userId = auth.currentUser?.uid,
                            uri = uri,
                            onSuccess = { url ->
                                resumeUrl = url
                                resumeUploaded = true
                                resumeProcessed = true
                                isUploading = false
                                onShowMessage("Resume processed! Fields auto-filled.")
                            },
                            onFailure = { error ->
                                isUploading = false
                                onShowMessage("Upload failed: $error")
                            }
                        )
                    } else {
                        isUploading = false
                        onShowMessage("Could not extract text from PDF")
                    }
                } catch (e: Exception) {
                    isUploading = false
                    onShowMessage("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return

        // Sirf Full Name required hai
        if (fullName.isBlank()) {
            onShowMessage("Please enter your full name")
            return
        }

        val skillList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val profileData = hashMapOf(
            "fullName" to fullName,
            "headline" to headline,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "skills" to skillList,
            "extractedSkills" to extractedSkills,
            "experienceYears" to experienceYears,
            "preferredRole" to preferredRole,
            "workPreference" to workPreference,
            "resumeUrl" to resumeUrl,
            "resumeText" to extractedText.take(5000),
            "linkedinUrl" to linkedinUrl,
            "portfolioUrl" to portfolioUrl,
            "profileCompleted" to true,
            "updatedAt" to System.currentTimeMillis()
        )

        isUploading = true

        // Use set() instead of update() - creates document if not exists
        db.collection("users").document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isUploading = false
                onShowMessage("Profile saved successfully!")
                onProfileComplete()
            }
            .addOnFailureListener { e ->
                isUploading = false
                onShowMessage("Failed to save: ${e.localizedMessage}")
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
                .alpha(entranceAlpha)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // STEP 1: RESUME UPLOAD (First)
            Text(
                text = "Upload Your Resume",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "We'll auto-fill your profile from resume",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Resume Upload Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isUploading) {
                        resumePicker.launch("application/pdf")
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(
                    2.dp,
                    if (resumeUploaded) PrimaryGreen else PrimaryGreen.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            isUploading -> "Processing Resume..."
                            resumeUploaded -> "✓ Resume Uploaded Successfully"
                            else -> "Tap to Upload Resume"
                        },
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (resumeUploaded) {
                            "We extracted your details automatically"
                        } else {
                            "PDF format • We'll extract your skills & details"
                        },
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }
            }

            // Show extracted info
            if (resumeProcessed) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✓ Auto-extracted from resume:",
                            color = PrimaryGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (extractedName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "👤 Name: $extractedName",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }

                        if (extractedEmail.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📧 Email: $extractedEmail",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }

                        if (extractedPhone.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📱 Phone: $extractedPhone",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }

                        if (extractedSkills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⭐ Skills: ${extractedSkills.take(8).joinToString(", ")}",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // STEP 2: REVIEW & EDIT (Shown after resume upload)
            if (resumeUploaded) {
                Text(
                    text = "Review Your Details",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name *",
                    placeholder = "John Doe",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = "Headline (Optional)",
                    placeholder = "Senior Android Developer",
                    icon = Icons.Default.Badge
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "john@example.com",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone (Optional)",
                    placeholder = "+91 98765 43210",
                    icon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location (Optional)",
                    placeholder = "Bengaluru, India",
                    icon = Icons.Default.LocationOn
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = "Skills (Optional)",
                    placeholder = "Kotlin, Java, Firebase",
                    icon = Icons.Default.Star
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = "Experience Years (Optional)",
                    placeholder = "5",
                    icon = Icons.Default.Timeline
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = preferredRole,
                    onValueChange = { preferredRole = it },
                    label = "Preferred Role (Optional)",
                    placeholder = "Android Developer",
                    icon = Icons.Default.Work
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Work Preference",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Remote", "Hybrid", "On-site").forEach { type ->
                        FilterChip(
                            selected = workPreference == type,
                            onClick = { workPreference = type },
                            label = { Text(type, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (workPreference == type) PrimaryGreen else Color(0xFF1A1A1A),
                                labelColor = if (workPreference == type) Color.Black else TextGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = linkedinUrl,
                    onValueChange = { linkedinUrl = it },
                    label = "LinkedIn URL (Optional)",
                    placeholder = "https://linkedin.com/in/johndoe",
                    icon = Icons.Default.Link
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = portfolioUrl,
                    onValueChange = { portfolioUrl = it },
                    label = "Portfolio URL (Optional)",
                    placeholder = "https://johndoe.dev",
                    icon = Icons.Default.Public
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = { saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Profile & Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Show hint if no resume uploaded yet
                Text(
                    text = "Upload your resume to get started.\nAll fields are optional except Full Name.",
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ====================================================================
// CUSTOM TEXT FIELD
// ====================================================================

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isReadOnly: Boolean = false
) {
    Column {
        Text(
            text = label,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = TextGray.copy(alpha = 0.4f))
            },
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = TextGray)
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A)
            ),
            readOnly = isReadOnly,
            singleLine = true
        )
    }
}

// ====================================================================
// PDF TEXT EXTRACTION
// ====================================================================

private fun extractTextFromPdf(context: android.content.Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val pdfReader = PdfReader(inputStream)
        val pdfDocument = PdfDocument(pdfReader)

        val textBuilder = StringBuilder()

        for (pageNum in 1..pdfDocument.numberOfPages) {
            val page = pdfDocument.getPage(pageNum)
            val text = PdfTextExtractor.getTextFromPage(page)
            textBuilder.append(text)
            textBuilder.append("\n")
        }

        pdfDocument.close()
        pdfReader.close()
        inputStream?.close()

        textBuilder.toString()
    } catch (e: Exception) {
        ""
    }
}

// ====================================================================
// EXTRACTION FUNCTIONS
// ====================================================================

private fun extractSkillsFromResume(text: String): List<String> {
    val skills = mutableListOf<String>()
    val techSkills = listOf(
        "Kotlin", "Java", "Android", "Flutter", "React Native", "Swift",
        "iOS", "JavaScript", "TypeScript", "React", "Node.js", "Python",
        "Django", "Flask", "Spring Boot", "Firebase", "AWS", "Azure",
        "GCP", "Docker", "Kubernetes", "SQL", "MySQL", "PostgreSQL",
        "MongoDB", "REST API", "GraphQL", "Git", "CI/CD", "Jetpack Compose",
        "MVVM", "MVP", "Clean Architecture", "Coroutines", "RxJava",
        "Dagger", "Hilt", "Retrofit", "OkHttp", "Unit Testing", "UI Testing",
        "HTML", "CSS", "PHP", "Laravel", "Vue.js", "Angular", ".NET"
    )

    val lowerText = text.lowercase()

    for (skill in techSkills) {
        if (lowerText.contains(skill.lowercase())) {
            skills.add(skill)
        }
    }

    return skills.distinct()
}

private fun extractNameFromResume(text: String): String {
    // Try to find name in first few lines
    val lines = text.lines().filter { it.isNotBlank() }

    for (i in 0 until minOf(5, lines.size)) {
        val line = lines[i].trim()
        // Name is usually short (2-3 words), no numbers, no special chars
        val words = line.split(" ")
        if (words.size in 2..3 &&
            line.matches(Regex("^[A-Za-z\\s.]+$")) &&
            !line.contains("@") &&
            !line.lowercase().contains("resume") &&
            !line.lowercase().contains("curriculum") &&
            !line.lowercase().contains("cv")
        ) {
            return line
        }
    }
    return ""
}

private fun extractEmailFromResume(text: String): String? {
    val emailRegex = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+")
    val match = emailRegex.find(text)
    return match?.value
}

private fun extractPhoneFromResume(text: String): String {
    val phoneRegex = Regex("(?:(?:\\+?\\d{1,3})?[-.\\s]?)?(?:\\(?\\d{3}\\)?[-.\\s]?)?\\d{3}[-.\\s]?\\d{4}")
    val match = phoneRegex.find(text)
    return match?.value?.trim() ?: ""
}

private fun extractLocationFromResume(text: String): String {
    val cities = listOf(
        "Bengaluru", "Bangalore", "Mumbai", "Delhi", "Noida", "Gurgaon",
        "Gurugram", "Pune", "Hyderabad", "Chennai", "Kolkata", "Ahmedabad",
        "Jaipur", "Remote", "India"
    )

    for (city in cities) {
        if (text.contains(city, ignoreCase = true)) {
            return city
        }
    }
    return ""
}

// ====================================================================
// UPLOAD RESUME
// ====================================================================

private fun uploadResume(
    storage: FirebaseStorage,
    userId: String?,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    if (userId == null) {
        onFailure("User not authenticated")
        return
    }

    val fileRef = storage.reference
        .child("resumes/$userId/${UUID.randomUUID()}.pdf")

    fileRef.putFile(uri)
        .addOnSuccessListener {
            fileRef.downloadUrl
                .addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
                .addOnFailureListener { e ->
                    onFailure(e.localizedMessage ?: "Failed to get download URL")
                }
        }
        .addOnFailureListener { e ->
            onFailure(e.localizedMessage ?: "Upload failed")
        }
}