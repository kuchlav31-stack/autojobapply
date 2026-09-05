package com.dark.jobai.ui.screens.onboarding

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EmailTemplateScreen(
    onTemplateSaved: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var autoSend by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    // Load existing template
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (doc.exists()) {
                    subject = doc.getString("emailSubjectTemplate") ?: ""
                    body = doc.getString("emailBodyTemplate") ?: ""
                    autoSend = doc.getBoolean("emailAutoSend") ?: true

                    if (subject.isBlank()) {
                        subject = "Application for {job_title} at {company}"
                    }
                    if (body.isBlank()) {
                        body = """Dear Hiring Manager,

My name is {applicant_name}. I am applying for the {job_title} position at {company}.

I believe my skills and experience make me a strong candidate.

Best regards,
{applicant_name}"""
                    }
                }
            } catch (e: Exception) {}
        }
        isLoading = false
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
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onTemplateSaved) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Email Template",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AppBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = AppBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Email Auto-Apply Template ✉️",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        "Customize your application email. Changes apply to all future auto-applies.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
                        lineHeight = 20.sp
                    )

                    // Subject Field
                    Text("Email Subject", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter subject template", color = TextMuted.copy(alpha = 0.6f), fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Body Field
                    Text("Email Body", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        placeholder = { Text("Enter body template", color = TextMuted.copy(alpha = 0.6f), fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Available Placeholders Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Available Placeholders:", color = AppBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "{job_title} - Job title\n{company} - Company name\n{applicant_name} - Your name",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Auto-send toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.03f))
                            .background(SurfaceWhite, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Send Mode", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Send email automatically on click", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = autoSend,
                            onCheckedChange = { autoSend = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BorderSubtle,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Save Button
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId == null) {
                                    isSaving = false
                                    return@launch
                                }

                                val templateData = mapOf(
                                    "emailSubjectTemplate" to subject,
                                    "emailBodyTemplate" to body,
                                    "emailAutoSend" to autoSend
                                )

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .set(templateData, com.google.firebase.firestore.SetOptions.merge())
                                    .addOnSuccessListener {
                                        isSaving = false
                                        Toast.makeText(context, "✅ Template saved!", Toast.LENGTH_SHORT).show()
                                        onTemplateSaved()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        Toast.makeText(context, "❌ ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save Template", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}