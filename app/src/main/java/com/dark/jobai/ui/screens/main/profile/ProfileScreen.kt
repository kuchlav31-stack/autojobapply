package com.dark.jobai.ui.screens.main.profile

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val ErrorRed = Color(0xFFEF4444)

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
                    if (enabled) "✅ Auto-Apply enabled!" else "Auto-Apply disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Account Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextDark
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(SurfaceWhite, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextDark, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgLight,
                    titleContentColor = TextDark
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppBlue, strokeWidth = 3.dp)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Profile not found", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Gorgeous Profile Header Card
                ProfileHeaderCard(user = user!!, isEmailVerified = isEmailVerified, onEditClick = onEditProfileClick)

                // 2. Interactive Stats Section
                ProfileStatsSection(user = user!!)

                // 3. Email Verification Banner (If needed)
                if (!isEmailVerified) {
                    EmailVerificationCard(
                        isVerificationSent = isVerificationSent,
                        onSendVerification = { sendVerificationEmail() },
                        onRefresh = {
                            scope.launch {
                                try {
                                    auth.currentUser?.reload()?.await()
                                    isEmailVerified = auth.currentUser?.isEmailVerified ?: false
                                    Toast.makeText(
                                        context,
                                        if (isEmailVerified) "✅ Email verified!" else "Not verified yet",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {}
                            }
                        }
                    )
                }

                // 4. Subscription Status with Expiry Countdown / Renewal Alert
                if (user!!.isPremiumActive()) {
                    ActiveSubscriptionCard(user = user!!, onRenewClick = onUpgradeClick)
                } else {
                    HighConversionJobBanner(onUpgradeClick = onUpgradeClick)
                }

                // 5. Quick Actions Card
                QuickActionsCard(
                    isPremium = user!!.isPremiumActive(),
                    isEmailVerified = isEmailVerified,
                    autoApplyEnabled = autoApplyEnabled,
                    onEmailTemplateClick = onEmailTemplateClick,
                    onEditProfileClick = onEditProfileClick,
                    onAutoApplyToggle = { toggleAutoApply(it) }
                )

                // 6. Professional Summary
                ProfileSectionCard(title = "Professional Summary", icon = Icons.Default.Description) {
                    Text(
                        text = user!!.bio.ifEmpty { "Add a bio to highlight your professional background and career goals." },
                        fontSize = 13.sp,
                        color = if (user!!.bio.isEmpty()) TextMuted else TextDark,
                        lineHeight = 20.sp
                    )
                }

                // 7. Skills & Expertise
                if (user!!.skills.isNotEmpty()) {
                    ProfileSectionCard(title = "Skills & Expertise", icon = Icons.Default.AutoAwesome) {
                        SkillsGrid(skills = user!!.skills)
                    }
                }

                // 8. Contact Information
                ProfileSectionCard(title = "Contact Information", icon = Icons.Default.Contacts) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoRow(Icons.Default.Email, "Email Address", user!!.email)
                        if (user!!.phone.isNotEmpty()) {
                            InfoRow(Icons.Default.Phone, "Phone Number", user!!.phone)
                        }
                        if (user!!.location.isNotEmpty()) {
                            InfoRow(Icons.Default.LocationOn, "Current Location", user!!.location)
                        }
                    }
                }

                // 9. Master Profile Details (CTC, Notice Period, Education)
                ProfileSectionCard(title = "Master ATS Profile Details", icon = Icons.Default.Badge) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (user!!.preferredRole.isNotEmpty()) {
                            InfoRow(Icons.Default.TrackChanges, "Target Role", user!!.preferredRole)
                        }
                        if (user!!.experienceYears.isNotEmpty()) {
                            InfoRow(Icons.Default.Timeline, "Experience", "${user!!.experienceYears} Years")
                        }
                        if (user!!.currentCompany.isNotEmpty()) {
                            InfoRow(Icons.Default.Business, "Current Company", user!!.currentCompany)
                        }
                        if (user!!.highestDegree.isNotEmpty()) {
                            InfoRow(Icons.Default.School, "Education", user!!.highestDegree)
                        }
                        if (user!!.currentCtc.isNotEmpty()) {
                            InfoRow(Icons.Default.Payments, "Current CTC", user!!.currentCtc)
                        }
                        if (user!!.expectedCtc.isNotEmpty()) {
                            InfoRow(Icons.Default.Savings, "Expected CTC", user!!.expectedCtc)
                        }
                        if (user!!.noticePeriod.isNotEmpty()) {
                            InfoRow(Icons.Default.Alarm, "Notice Period", user!!.noticePeriod)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 10. Logout Button
                Button(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceWhite,
                        contentColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ====================================================================
// 1. PROFILE HEADER CARD
// ====================================================================

@Composable
fun ProfileHeaderCard(user: User, isEmailVerified: Boolean, onEditClick: () -> Unit) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val GoldPremium = Color(0xFFD97706)
    val InfoBlue = Color(0xFF3B82F6)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = AppBlue.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                if (user.isPremiumActive())
                                    listOf(GoldPremium, Color(0xFFFBBF24), GoldPremium)
                                else
                                    listOf(AppBlue, Color(0xFF60A5FA), AppBlue)
                            )
                        )
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            user.fullName.take(1).uppercase(),
                            color = if (user.isPremiumActive()) GoldPremium else AppBlue,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            user.fullName.ifEmpty { "Guest User" },
                            color = TextDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isEmailVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = InfoBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (user.headline.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            user.headline,
                            color = AppBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            user.location.ifEmpty { "Location not set" },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Icon(Icons.Default.Edit, null, tint = TextDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Master Profile & Resume", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ====================================================================
// 2. PROFILE STATS SECTION
// ====================================================================

@Composable
fun ProfileStatsSection(user: User) {
    val SurfaceWhite = Color(0xFFFFFFFF)
    val BorderSubtle = Color(0xFFE2E8F0)
    val AppBlue = Color(0xFF0F52FF)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatBox(user.emailsSent.toString(), "Applications", Icons.Default.Send, AppBlue)
            VerticalDivider(color = BorderSubtle, modifier = Modifier.height(28.dp))
            StatBox(user.getRemainingEmails().toString(), "Remaining", Icons.Default.Mail, AppBlue)
            VerticalDivider(color = BorderSubtle, modifier = Modifier.height(28.dp))
            StatBox(user.skills.size.toString(), "Skills", Icons.Default.Bolt, AppBlue)
        }
    }
}

@Composable
fun StatBox(value: String, label: String, icon: ImageVector, color: Color) {
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ====================================================================
// 3. EMAIL VERIFICATION CARD
// ====================================================================

@Composable
fun EmailVerificationCard(
    isVerificationSent: Boolean,
    onSendVerification: () -> Unit,
    onRefresh: () -> Unit
) {
    val WarningOrange = Color(0xFFF59E0B)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val SurfaceWhite = Color(0xFFFFFFFF)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = WarningOrange.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Email Verification Required",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Please verify your email address to enable AI Auto-Apply features.",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSendVerification,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isVerificationSent,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isVerificationSent) "Verification Sent" else "Verify Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f).height(40.dp),
                    border = BorderStroke(1.dp, WarningOrange),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite, contentColor = WarningOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("I've Verified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ====================================================================
// 4. ACTIVE SUBSCRIPTION WITH EXPIRY COUNTDOWN
// ====================================================================

@Composable
fun ActiveSubscriptionCard(user: User, onRenewClick: () -> Unit) {
    val GoldPremium = Color(0xFFD97706)
    val TextDark = Color(0xFF0F172A)
    val SurfaceWhite = Color(0xFFFFFFFF)

    // Calculate remaining days if premiumUntil exists
    val currentTime = System.currentTimeMillis()
    val expiryTime = user.premiumExpiry
    val diffDays = if (expiryTime > currentTime) ((expiryTime - currentTime) / (1000 * 60 * 60 * 24)).toInt() else 0

    val isExpiringSoon = diffDays in 0..3

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, if (isExpiringSoon) Color(0xFFEF4444) else GoldPremium.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (isExpiringSoon)
                            listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2))
                        else
                            listOf(Color(0xFFFFFAEB), Color(0xFFFEF3C7))
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background((if (isExpiringSoon) Color(0xFFEF4444) else GoldPremium).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpiringSoon) Icons.Default.Warning else Icons.Default.Verified,
                    contentDescription = null,
                    tint = if (isExpiringSoon) Color(0xFFEF4444) else GoldPremium,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isExpiringSoon) "⚠️ Plan Expiring in $diffDays Days!" else "Active Plan: ${user.premiumPlan.uppercase()} ⭐",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isExpiringSoon) "Renew now to avoid service interruption." else "Unlimited AI Auto-Apply & Priority Queue enabled.",
                    color = if (isExpiringSoon) Color(0xFF991B1B) else GoldPremium,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isExpiringSoon) {
                Button(
                    onClick = onRenewClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Renew", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ====================================================================
// 4b. HIGH CONVERSION UPGRADE BANNER
// ====================================================================

@Composable
fun HighConversionJobBanner(onUpgradeClick: () -> Unit) {
    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = AppBlue.copy(alpha = 0.1f))
            .clickable(onClick = onUpgradeClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.5.dp, AppBlue.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AppBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WorkspacePremium, null, tint = AppBlue, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Land Interviews 5x Faster 🚀",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Unlock AI Auto-Apply & 500+ monthly applications today!",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Icon(Icons.Default.ChevronRight, null, tint = AppBlue, modifier = Modifier.size(20.dp))
        }
    }
}

// ====================================================================
// 5. QUICK ACTIONS CARD
// ====================================================================

@Composable
fun QuickActionsCard(
    isPremium: Boolean,
    isEmailVerified: Boolean,
    autoApplyEnabled: Boolean,
    onEmailTemplateClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAutoApplyToggle: (Boolean) -> Unit
) {
    val AppBlue = Color(0xFF0F52FF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    ProfileSectionCard(title = "Quick Actions", icon = Icons.Default.Bolt) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.SmartToy, null, tint = AppBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI Auto-Apply", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (!isPremium) "Requires Premium Plan" else if (!isEmailVerified) "Verify Email Required" else "Auto-apply to matching jobs",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = autoApplyEnabled,
                    onCheckedChange = onAutoApplyToggle,
                    enabled = isPremium && isEmailVerified,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppBlue,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable(onClick = onEmailTemplateClick)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MailOutline, null, tint = AppBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Email Templates", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ====================================================================
// SHARED SECTION CARD
// ====================================================================

@Composable
fun ProfileSectionCard(
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
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(icon, null, tint = AppBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
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

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    val AppBlue = Color(0xFF0F52FF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = AppBlue, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                value,
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsGrid(skills: List<String>) {
    val AppBlue = Color(0xFF0F52FF)

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        skills.forEach { skill ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBlue.copy(alpha = 0.08f))
                    .border(1.dp, AppBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    skill,
                    color = AppBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}