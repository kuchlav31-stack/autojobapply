package com.dark.jobai.ui.screens.main.profile

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.dark.jobai.data.model.User
import com.dark.jobai.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ====================================================================
// EXACT COLORS FROM APPLYAI SCREENSHOT
// ====================================================================
private val ApplyAiBlue = Color(0xFF0066FF)            // Signature Blue Button & Logo
private val ApplyAiBlueSoft = Color(0xFFEBF3FF)        // Light Blue Container / Selected Tab
private val ApplyAiCanvas = Color(0xFFF6F9FE)          // Soft Sky Background
private val ApplyAiSurface = Color(0xFFFFFFFF)         // Clean Pure White Card
private val ApplyAiGreen = Color(0xFF00C781)           // Concentric Ring Checkmark Green
private val ApplyAiGreenSoft = Color(0xFFE7F9F2)       // Mint Soft Halo
private val ApplyAiCoral = Color(0xFFFF5274)           // Notification Pink/Coral
private val ApplyAiCoralSoft = Color(0xFFFFF0F3)
private val ApplyAiAmber = Color(0xFFFFAB00)           // Bell Notification Amber
private val ApplyAiAmberSoft = Color(0xFFFFF7E6)
private val ApplyAiTextDark = Color(0xFF161C30)        // Deep Title Navy
private val ApplyAiTextSub = Color(0xFF6B7897)         // Supporting Gray Text
private val ApplyAiBorder = Color(0xFFE3EDFB)          // Subtle Line Border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onEmailTemplateClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val user by profileViewModel.user.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    var isEmailVerified by remember { mutableStateOf(auth.currentUser?.isEmailVerified ?: false) }
    var isVerificationSent by remember { mutableStateOf(false) }
    var autoApplyEnabled by remember { mutableStateOf(false) }

    // Segmented Navigation: 0 = Overview, 1 = ATS Resume, 2 = AI Auto-Apply
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            autoApplyEnabled = user!!.autoApplyEnabled
        }
    }

    LaunchedEffect(Unit) {
        try {
            auth.currentUser?.reload()?.await()
            isEmailVerified = auth.currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {}
    }

    fun sendVerificationEmail() {
        scope.launch {
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                isVerificationSent = true
                Toast.makeText(context, "✅ Verification email sent!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "❌ ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleAutoApply(enabled: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        if (enabled && !isEmailVerified) {
            Toast.makeText(context, "Please verify your email first", Toast.LENGTH_LONG).show()
            return
        }

        autoApplyEnabled = enabled

        db.collection("users").document(userId)
            .update("autoApplyEnabled", enabled)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    if (enabled) "⚡ ApplyAI Auto-Apply active!" else "Auto-Apply paused",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ApplyAiCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Apply",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ApplyAiTextDark
                        )
                        Text(
                            "AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ApplyAiBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ApplyAiSurface)
                            .border(1.dp, ApplyAiBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = ApplyAiTextSub, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ApplyAiCanvas
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApplyAiBlue, strokeWidth = 3.dp)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile not found", color = ApplyAiTextSub, fontSize = 15.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 1. CONCENTRIC RING HERO AVATAR (Screen 10 "You're All Set" style)
                ApplyAiConcentricHero(
                    user = user!!,
                    isEmailVerified = isEmailVerified
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. SEGMENTED TABS (Screen 8 "Job Type / Work Mode" style)
                ApplyAiSegmentedTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. TAB CONTENT VIEWS (Replaces endless box scroll!)
                AnimatedContent(
                    targetState = selectedTab,
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        0 -> OverviewTabView(
                            user = user!!,
                            isEmailVerified = isEmailVerified,
                            onUpgradeClick = onUpgradeClick,
                            onSendVerification = { sendVerificationEmail() }
                        )
                        1 -> AtsResumeTabView(
                            user = user!!,
                            onEditClick = onEditProfileClick
                        )
                        2 -> AutoPilotTabView(
                            user = user!!,
                            isEmailVerified = isEmailVerified,
                            autoApplyEnabled = autoApplyEnabled,
                            onAutoApplyToggle = { toggleAutoApply(it) },
                            onEmailTemplateClick = onEmailTemplateClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 4. LOGOUT BUTTON (Clean pill style)
                Button(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApplyAiSurface,
                        contentColor = Color(0xFFEF4444)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFD5D5)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ====================================================================
// 1. CONCENTRIC RING HERO AVATAR (Screen 10 Style)
// ====================================================================

@Composable
fun ApplyAiConcentricHero(user: User, isEmailVerified: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Outer concentric halo
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(if (isEmailVerified) ApplyAiGreenSoft else ApplyAiBlueSoft)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Mid ring
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(ApplyAiSurface)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile image core
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ApplyAiBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            color = ApplyAiBlue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = user.fullName.ifEmpty { "Candidate" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ApplyAiTextDark
            )
            if (isEmailVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = ApplyAiGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = user.headline.ifEmpty { "Job Seeker" },
            fontSize = 13.sp,
            color = ApplyAiBlue,
            fontWeight = FontWeight.SemiBold
        )

        if (user.location.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = user.location,
                fontSize = 12.sp,
                color = ApplyAiTextSub
            )
        }
    }
}

// ====================================================================
// 2. SEGMENTED TABS (Screen 8 Capsule Button Style)
// ====================================================================

@Composable
fun ApplyAiSegmentedTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Overview", "ATS Resume", "AI Pilot")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(ApplyAiSurface)
            .border(1.dp, ApplyAiBorder, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) ApplyAiBlue else Color.Transparent)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else ApplyAiTextSub,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ====================================================================
// 3A. OVERVIEW TAB: FLOATING NOTIFICATIONS & SKILLS (Screen 4 Style)
// ====================================================================

@Composable
fun OverviewTabView(
    user: User,
    isEmailVerified: Boolean,
    onUpgradeClick: () -> Unit,
    onSendVerification: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Verification prompt if not verified
        if (!isEmailVerified) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ApplyAiAmberSoft)
                    .border(1.dp, ApplyAiAmber.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ErrorOutline, null, tint = ApplyAiAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Verify your email to start auto-applying", fontSize = 12.sp, color = ApplyAiTextDark, modifier = Modifier.weight(1f))
                TextButton(onClick = onSendVerification) {
                    Text("Verify", color = ApplyAiBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Screen 4 Style: Floating Updates Cards
        ApplyAiNotificationPill(
            icon = Icons.Default.Send,
            iconColor = ApplyAiBlue,
            iconBg = ApplyAiBlueSoft,
            title = "${user.emailsSent} applications submitted",
            subtitle = "Tracked in real-time"
        )

        ApplyAiNotificationPill(
            icon = Icons.Default.CalendarToday,
            iconColor = ApplyAiCoral,
            iconBg = ApplyAiCoralSoft,
            title = "${user.getRemainingEmails()} applications remaining",
            subtitle = "Current monthly cycle"
        )

        ApplyAiNotificationPill(
            icon = Icons.Default.NotificationsNone,
            iconColor = ApplyAiAmber,
            iconBg = ApplyAiAmberSoft,
            title = "${user.skills.size} matched skills configured",
            subtitle = "Ready for job matching"
        )

        // Screen 5 Style: Autopilot Upgrade Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUpgradeClick),
            shape = RoundedCornerShape(20.dp),
            color = ApplyAiBlue
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (user.isPremiumActive()) "Plan: ${user.premiumPlan.uppercase()} ⭐" else "Your Career, On Autopilot",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (user.isPremiumActive()) "Priority application queue enabled" else "Less effort. More opportunities.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Bio Section
        if (user.bio.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ApplyAiSurface)
                    .border(1.dp, ApplyAiBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Text("Candidate Summary", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(user.bio, fontSize = 12.sp, color = ApplyAiTextSub, lineHeight = 19.sp)
            }
        }
    }
}

// Floating notification pill (Screen 4 exact layout)
@Composable
fun ApplyAiNotificationPill(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = ApplyAiBlue.copy(alpha = 0.03f))
            .clip(RoundedCornerShape(18.dp))
            .background(ApplyAiSurface)
            .border(1.dp, ApplyAiBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
            Text(subtitle, fontSize = 11.sp, color = ApplyAiTextSub)
        }
    }
}

// ====================================================================
// 3B. ATS RESUME TAB: RESUME CARD & PREFERENCES (Screen 8 & 9 Style)
// ====================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AtsResumeTabView(
    user: User,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen 9 Style: Uploaded Resume Sheet Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ApplyAiSurface)
                .border(1.dp, ApplyAiBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFECEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = ApplyAiCoral, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.fullName.ifEmpty { "My" }}_Resume.pdf",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApplyAiTextDark
                )
                Text("Master ATS Profile Active", fontSize = 11.sp, color = ApplyAiGreen, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.CheckCircle, null, tint = ApplyAiGreen, modifier = Modifier.size(20.dp))
        }

        // Screen 8 Style: Preferred Roles & Skills Tags
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ApplyAiSurface)
                .border(1.dp, ApplyAiBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text("Preferred Roles & Skills", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.preferredRole.isNotEmpty()) {
                    ApplyAiRemovableTag(user.preferredRole)
                }
                user.skills.forEach { skill ->
                    ApplyAiRemovableTag(skill)
                }
            }
        }

        // Screen 8 Style: ATS Specifications Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ApplyAiSurface)
                .border(1.dp, ApplyAiBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Job Matching Attributes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
            Spacer(modifier = Modifier.height(2.dp))

            ApplyAiDetailItem("Experience", "${user.experienceYears.ifEmpty { "0" }} Years")
            ApplyAiDetailItem("Current Company", user.currentCompany.ifEmpty { "Not specified" })
            ApplyAiDetailItem("Highest Degree", user.highestDegree.ifEmpty { "Not specified" })
            ApplyAiDetailItem("Current Package", user.currentCtc.ifEmpty { "Not specified" })
            ApplyAiDetailItem("Expected Package", user.expectedCtc.ifEmpty { "Not specified" })
            ApplyAiDetailItem("Notice Period", user.noticePeriod.ifEmpty { "Immediate" })
            ApplyAiDetailItem("Email", user.email)
            if (user.phone.isNotEmpty()) ApplyAiDetailItem("Phone", user.phone)
        }

        // Screen 8 Style Blue "Next / Edit" button
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ApplyAiBlue)
        ) {
            Text("Edit Preferences & Resume", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

// Removable/Capsule style chip from Screen 8
@Composable
fun ApplyAiRemovableTag(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ApplyAiBlueSoft)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 12.sp, color = ApplyAiBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ApplyAiDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = ApplyAiTextSub)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
    }
}

// ====================================================================
// 3C. AI PILOT TAB: AUTO-APPLY DOCUMENT (Screen 3 Style)
// ====================================================================

@Composable
fun AutoPilotTabView(
    user: User,
    isEmailVerified: Boolean,
    autoApplyEnabled: Boolean,
    onAutoApplyToggle: (Boolean) -> Unit,
    onEmailTemplateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen 3 Style: Interactive "Auto Apply for You" Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = ApplyAiBlue.copy(alpha = 0.06f))
                .clip(RoundedCornerShape(22.dp))
                .background(ApplyAiSurface)
                .border(1.dp, ApplyAiBorder, RoundedCornerShape(22.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (autoApplyEnabled) ApplyAiGreenSoft else ApplyAiBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (autoApplyEnabled) Icons.Default.CheckCircle else Icons.Default.Send,
                    contentDescription = null,
                    tint = if (autoApplyEnabled) ApplyAiGreen else ApplyAiBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Auto Apply for You", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Sit back while ApplyAI automatically applies to the best matching jobs for your profile.",
                fontSize = 12.sp,
                color = ApplyAiTextSub,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Large Apply Pill / Switch Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApplyAiCanvas)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (autoApplyEnabled) "Autopilot Active" else "Autopilot Paused",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApplyAiTextDark
                    )
                    Text(
                        when {
                            !user.isPremiumActive() -> "Requires Membership"
                            !isEmailVerified -> "Verify Email Required"
                            else -> "Running background applications"
                        },
                        fontSize = 11.sp,
                        color = ApplyAiTextSub
                    )
                }

                Switch(
                    checked = autoApplyEnabled,
                    onCheckedChange = onAutoApplyToggle,
                    enabled = user.isPremiumActive() && isEmailVerified,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ApplyAiBlue,
                        uncheckedTrackColor = ApplyAiBorder
                    )
                )
            }
        }

        // Email Templates Button Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ApplyAiSurface)
                .border(1.dp, ApplyAiBorder, RoundedCornerShape(18.dp))
                .clickable(onClick = onEmailTemplateClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApplyAiBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MailOutline, null, tint = ApplyAiBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text("Customize Email Pitch Template", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApplyAiTextDark)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = ApplyAiTextSub, modifier = Modifier.size(18.dp))
        }
    }
}