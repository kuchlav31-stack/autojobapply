package com.dark.autojobapply

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.ui.theme.BackgroundDark
import com.dark.jobai.ui.theme.BorderGray
import com.dark.jobai.ui.theme.ErrorRed
import com.dark.jobai.ui.theme.PrimaryGreen
import com.dark.jobai.ui.theme.TextGray
import com.dark.jobai.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    onBack: () -> Unit = {},
    onPaymentSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var isPremium by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    var premiumExpiry by remember { mutableStateOf(0L) }
    var emailsSent by remember { mutableStateOf(0L) }
    var emailLimit by remember { mutableStateOf(0L) }

    // Check premium status
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        isPremium = doc.getBoolean("isPremium") ?: false
                        premiumExpiry = doc.getLong("premiumExpiry") ?: 0L
                        emailsSent = doc.getLong("emailsSent") ?: 0L
                        emailLimit = doc.getLong("emailLimit") ?: 0L
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Premium Plans",
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
                    .padding(16.dp)
            ) {
                // Current Status
                if (isPremium) {
                    PremiumStatusCard(
                        isPremium = true,
                        premiumExpiry = premiumExpiry,
                        emailsSent = emailsSent,
                        emailLimit = emailLimit
                    )
                } else {
                    PremiumStatusCard(
                        isPremium = false,
                        premiumExpiry = 0L,
                        emailsSent = 0L,
                        emailLimit = 0L
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Choose Your Plan",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Premium Plan
                PlanCard(
                    planName = "Premium",
                    price = "₹2",
                    period = "/month",
                    description = "For serious job seekers",
                    features = listOf(
                        "50 Email Auto-Apply/month",
                        "Email Tracking (Opened/Delivered)",
                        "Application Dashboard",
                        "Custom Email Template",
                        "AI Job Matching Score",
                        "Priority Support"
                    ),
                    isRecommended = true,
                    gradientColors = listOf(PrimaryGreen, Color(0xFF00D2A0)),
                    onClick = {
                        if (activity != null) {
                            val razorpay = RazorpayManager(activity, context)
                            razorpay.startPayment(
                                planId = "premium_monthly",
                                planName = "Premium",
                                amount = 299,
                                onSuccess = {
                                    onPaymentSuccess()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pro Plan
                PlanCard(
                    planName = "Pro",
                    price = "₹599",
                    period = "/month",
                    description = "For active job seekers",
                    features = listOf(
                        "200 Email Auto-Apply/month",
                        "Auto-Apply Mode (Daily 10)",
                        "AI Generated Cover Letter",
                        "Recruiter Outreach",
                        "3 Multiple Resumes",
                        "Advanced Analytics",
                        "Priority Support"
                    ),
                    isRecommended = false,
                    gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)),
                    onClick = {
                        if (activity != null) {
                            val razorpay = RazorpayManager(activity, context)
                            razorpay.startPayment(
                                planId = "pro_monthly",
                                planName = "Pro",
                                amount = 599,
                                onSuccess = {
                                    onPaymentSuccess()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Agency Plan
                PlanCard(
                    planName = "Agency",
                    price = "₹1499",
                    period = "/month",
                    description = "For agencies & recruiters",
                    features = listOf(
                        "1000 Email Auto-Apply/month",
                        "Auto-Apply Mode (Daily 50)",
                        "Bulk Apply (50 jobs at once)",
                        "10 Multiple Resumes",
                        "3 Team Members",
                        "API Access",
                        "Dedicated Support"
                    ),
                    isRecommended = false,
                    gradientColors = listOf(Color(0xFFFFA726), Color(0xFFF57C00)),
                    onClick = {
                        if (activity != null) {
                            val razorpay = RazorpayManager(activity, context)
                            razorpay.startPayment(
                                planId = "agency_monthly",
                                planName = "Agency",
                                amount = 1499,
                                onSuccess = {
                                    onPaymentSuccess()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lifetime Deal
                LifetimeDealCard(
                    onClick = {
                        Toast.makeText(context, "Contact support for lifetime deals!", Toast.LENGTH_LONG).show()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ====================================================================
// PREMIUM STATUS CARD
// ====================================================================

@Composable
fun PremiumStatusCard(
    isPremium: Boolean,
    premiumExpiry: Long,
    emailsSent: Long,
    emailLimit: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFF1A3A2A) else Color(0xFF1A1A1A)
        ),
        border = BorderStroke(
            2.dp,
            if (isPremium) PrimaryGreen else BorderGray
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isPremium) Icons.Default.Star else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isPremium) Color(0xFFFFD700) else TextGray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (isPremium) "Premium Active" else "Free Plan",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isPremium) "Expires: ${formatDate(premiumExpiry)}" else "Upgrade to unlock premium features",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            if (isPremium) {
                Spacer(modifier = Modifier.height(16.dp))

                // Email usage progress
                val progress = if (emailLimit > 0) {
                    (emailsSent.toFloat() / emailLimit.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Text(
                    "Email Usage: $emailsSent / $emailLimit",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryGreen,
                    trackColor = Color(0xFF2A2A2A)
                )
            }
        }
    }
}

// ====================================================================
// PLAN CARD
// ====================================================================

@Composable
fun PlanCard(
    planName: String,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    isRecommended: Boolean,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(
            2.dp,
            if (isRecommended) PrimaryGreen else BorderGray.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Recommended Badge
            if (isRecommended) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryGreen)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "MOST POPULAR",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Plan Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        planName,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        price,
                        color = gradientColors[0],
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        period,
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Features
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        feature,
                        color = TextWhite.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = gradientColors[0],
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Choose $planName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ====================================================================
// LIFETIME DEAL CARD
// ====================================================================

@Composable
fun LifetimeDealCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        border = BorderStroke(2.dp, Color(0xFFFFD700))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Lifetime Deal",
                color = Color(0xFFFFD700),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Pay once, use forever!",
                color = TextGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("₹999", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("₹2999", color = TextGray, fontSize = 16.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Limited time offer - 66% OFF",
                color = ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ====================================================================
// HELPER
// ====================================================================

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return format.format(date)
}