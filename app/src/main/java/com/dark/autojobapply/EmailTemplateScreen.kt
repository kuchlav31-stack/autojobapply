package com.dark.autojobapply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.ui.theme.BackgroundDark
import com.dark.jobai.ui.theme.BorderGray
import com.dark.jobai.ui.theme.PrimaryGreen
import com.dark.jobai.ui.theme.TextGray
import com.dark.jobai.ui.theme.TextWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTemplateScreen(
    onTemplateSaved: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val emailManager = remember { EmailApplicationManager(context) }
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("Application for {job_title} at {company}") }
    var body by remember { mutableStateOf("""Dear Hiring Manager,

My name is {applicant_name}. I am applying for the {job_title} position at {company}.

I believe my skills and experience make me a strong candidate.

Best regards,
{applicant_name}""") }

    var autoSend by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
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
            "Email Auto-Apply Setup",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Set once, apply everywhere with one click!",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Text("Email Subject", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Email Body", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = PrimaryGreen,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto-Send Mode", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Email sent automatically", color = TextGray, fontSize = 12.sp)
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

        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    emailManager.saveTemplateToServer(
                        subjectTemplate = subject,
                        bodyTemplate = body,
                        autoSend = autoSend,
                        onSuccess = {
                            isSaving = false
                            onShowMessage("✅ Email template saved!")
                            onTemplateSaved()
                        },
                        onError = { error ->
                            isSaving = false
                            onShowMessage("❌ $error")
                        }
                    )
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
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Template", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}