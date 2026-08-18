package com.dark.jobai.ui.screens.main.profile

import android.widget.Toast
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.User
import com.dark.jobai.ui.theme.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Profile",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Settings, "Settings", tint = TextGray)
                }
                IconButton(onClick = onEditProfileClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = PrimaryGreen)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile not found", color = TextGray, fontSize = 16.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile Header
                ProfileHeader(user = user!!, isEmailVerified = isEmailVerified)

                Spacer(modifier = Modifier.height(20.dp))

                // Email Verification
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
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Quick Actions
                QuickActionsCard(
                    isPremium = user!!.isPremiumActive(),
                    isEmailVerified = isEmailVerified,
                    autoApplyEnabled = autoApplyEnabled,
                    onEmailTemplateClick = onEmailTemplateClick,
                    onEditProfileClick = onEditProfileClick,
                    onUpgradeClick = onUpgradeClick,
                    onAutoApplyToggle = { toggleAutoApply(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Status
                if (user!!.isPremiumActive()) {
                    PremiumStatusCard(user = user!!)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Stats
                ProfileStats(user = user!!)

                Spacer(modifier = Modifier.height(20.dp))

                // Skills - FIXED with simple rows
                if (user!!.skills.isNotEmpty()) {
                    SectionCard(title = "Skills", icon = Icons.Default.Star) {
                        SkillsGrid(skills = user!!.skills)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Contact Info
                SectionCard(title = "Contact Information", icon = Icons.Default.Contacts) {
                    InfoRow(Icons.Default.Email, "Email", user!!.email)
                    if (user!!.phone.isNotEmpty()) {
                        InfoRow(Icons.Default.Phone, "Phone", user!!.phone)
                    }
                    if (user!!.location.isNotEmpty()) {
                        InfoRow(Icons.Default.LocationOn, "Location", user!!.location)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Job Preferences
                SectionCard(title = "Job Preferences", icon = Icons.Default.Work) {
                    if (user!!.preferredRole.isNotEmpty()) {
                        InfoRow(Icons.Default.Badge, "Preferred Role", user!!.preferredRole)
                    }
                    if (user!!.experienceYears.isNotEmpty()) {
                        InfoRow(Icons.Default.Timeline, "Experience", "${user!!.experienceYears} years")
                    }
                    InfoRow(Icons.Default.Business, "Work Type", user!!.workPreference)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout
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
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ====================================================================
// SKILLS GRID (Fixed - No FlowRow needed)
// ====================================================================

@Composable
fun SkillsGrid(skills: List<String>) {
    val chunkedSkills = skills.take(9).chunked(3)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunkedSkills.forEach { rowSkills ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSkills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            skill,
                            color = PrimaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// PROFILE HEADER
// ====================================================================

@Composable
fun ProfileHeader(user: User, isEmailVerified: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (user.isPremiumActive())
                            listOf(GoldPremium, Color(0xFFFFA000))
                        else
                            listOf(PrimaryGreen, Color(0xFF00D2A0))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    user.fullName.take(1).uppercase(),
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    user.fullName.ifEmpty { "Your Name" },
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isEmailVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = InfoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (user.headline.isNotEmpty()) {
                Text(user.headline, color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            if (user.location.isNotEmpty()) {
                Text("📍 ${user.location}", color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

// ====================================================================
// EMAIL VERIFICATION CARD
// ====================================================================

@Composable
fun EmailVerificationCard(
    isVerificationSent: Boolean,
    onSendVerification: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = WarningOrange, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email Not Verified", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Verify your email to unlock all features", color = TextGray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSendVerification,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (isVerificationSent) "Resend Email" else "Send Verification",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, InfoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("I've Verified", fontSize = 12.sp, color = InfoBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ====================================================================
// QUICK ACTIONS
// ====================================================================

@Composable
fun QuickActionsCard(
    isPremium: Boolean,
    isEmailVerified: Boolean,
    autoApplyEnabled: Boolean,
    onEmailTemplateClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onAutoApplyToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "Quick Actions",
                color = TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            // Auto-Apply (only for premium + verified)
            if (isPremium && isEmailVerified) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (autoApplyEnabled) SuccessGreen.copy(alpha = 0.15f) else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = if (autoApplyEnabled) SuccessGreen else TextGray, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Apply", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Daily 10 matching jobs auto-applied", color = TextGray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoApplyEnabled,
                        onCheckedChange = onAutoApplyToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = SuccessGreen)
                    )
                }
                HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))
            }

            QuickActionRow(Icons.Default.Email, "Email Template", "Edit your auto-apply template", PrimaryGreen, onEmailTemplateClick)
            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))
            QuickActionRow(Icons.Default.Edit, "Edit Profile", "Update your information", InfoBlue, onEditProfileClick)

            if (!isPremium) {
                HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))
                QuickActionRow(Icons.Default.Star, "Upgrade to Premium", "Unlock all features", GoldPremium, onUpgradeClick)
            }
        }
    }
}

@Composable
fun QuickActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextGray, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextGray, modifier = Modifier.size(20.dp))
    }
}

// ====================================================================
// PREMIUM STATUS
// ====================================================================

@Composable
fun PremiumStatusCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A2A)),
        border = BorderStroke(2.dp, GoldPremium)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = GoldPremium, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Premium Active", color = GoldPremium, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (user.emailLimit > 0) (user.emailsSent.toFloat() / user.emailLimit.toFloat()).coerceIn(0f, 1f) else 0f

            Text("Email Usage: ${user.emailsSent} / ${user.emailLimit}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = GoldPremium,
                trackColor = SurfaceElevated
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Remaining: ${user.getRemainingEmails()} emails", color = TextGray, fontSize = 11.sp)
        }
    }
}

// ====================================================================
// STATS
// ====================================================================

@Composable
fun ProfileStats(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceDark).padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(user.emailLimit.toString(), "Email Limit")
        VerticalDivider(color = BorderGray, modifier = Modifier.height(30.dp))
        StatItem(user.emailsSent.toString(), "Emails Sent")
        VerticalDivider(color = BorderGray, modifier = Modifier.height(30.dp))
        StatItem(user.skills.size.toString(), "Skills")
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = PrimaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextGray, fontSize = 10.sp)
    }
}

// ====================================================================
// SECTION CARD
// ====================================================================

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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

// ====================================================================
// INFO ROW
// ====================================================================

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
            Text(value, color = TextWhite, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}