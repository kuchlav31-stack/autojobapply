package com.dark.jobai.ui.screens.onboarding

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
import com.dark.jobai.ui.theme.*
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

                    // If empty, use defaults
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTemplateSaved) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Email Template",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Email Auto-Apply Template",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Edit your template. Changes apply to all future applications.",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Subject
                Text("Email Subject", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter subject template") },
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

                Spacer(modifier = Modifier.height(16.dp))

                // Body
                Text("Email Body", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    placeholder = { Text("Enter body template") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = PrimaryGreen,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Placeholders
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Available Placeholders:", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "{job_title} - Job title\n{company} - Company name\n{applicant_name} - Your name",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Auto-send toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Send Mode", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Email sent automatically on click", color = TextGray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoSend,
                        onCheckedChange = { autoSend = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = {
                        isSaving = true

                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button

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
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Save Template", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}