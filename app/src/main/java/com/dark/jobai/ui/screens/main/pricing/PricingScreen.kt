package com.dark.jobai.ui.screens.main.pricing

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

data class PricingPlan(
    val id: String,         // "free", "starter", "pro", "unlimited"
    val name: String,
    val price: String,
    val rawAmount: Int,     // in paise for Razorpay
    val level: Int,         // 0: Free, 1: Starter, 2: Pro, 3: Unlimited
    val period: String,
    val subtitle: String,
    val description: String,
    val features: List<String>,
    val backgroundBrush: Brush,
    val textColor: Color,
    val cardColor: Color,
    val buttonColor: Color,
    val isPopular: Boolean = false,
    val badgeText: String? = null
)

@Composable
fun PricingScreen(
    onUpgradeSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var userActivePlan by remember { mutableStateOf("free") }
    var premiumExpiry by remember { mutableStateOf(0L) }
    var isLoadingUserPlan by remember { mutableStateOf(true) }

    // Fetch user active plan from Firestore
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .get()
                    .await()
                if (doc.exists()) {
                    userActivePlan = doc.getString("premiumPlan") ?: "free"
                    premiumExpiry = doc.getLong("premiumExpiry") ?: doc.getLong("premiumUntil") ?: 0L
                }
            } catch (e: Exception) {}
        }
        isLoadingUserPlan = false
    }

    LaunchedEffect(Unit) {
        Checkout.preload(context)
    }

    fun getPlanLevel(id: String): Int {
        return when (id) {
            "starter" -> 1
            "pro" -> 2
            "unlimited" -> 3
            else -> 0
        }
    }

    // Razorpay Prorated Payment Trigger
    fun startRazorpayPayment(plan: PricingPlan) {
        val activeLevel = getPlanLevel(userActivePlan)
        val targetLevel = getPlanLevel(plan.id)

        if (targetLevel <= activeLevel) {
            Toast.makeText(context, "You already own this or a higher plan!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPlanPricePaise = when (userActivePlan) {
            "starter" -> 29900
            "pro" -> 69900
            else -> 0
        }

        var finalAmountPaise = plan.rawAmount - currentPlanPricePaise
        if (finalAmountPaise < 100) finalAmountPaise = 100 // Minimum ₹50 safeguard

        val descriptionText = if (activeLevel > 0) "Upgrade to ${plan.name} (Difference)" else "Subscription for ${plan.name}"

        val checkout = Checkout()
        checkout.setKeyID("rzp_live_6vd9RApruseTAi")

        try {
            val options = JSONObject().apply {
                put("name", "JobAI Career Agent")
                put("description", descriptionText)
                put("currency", "INR")
                put("amount", finalAmountPaise)
                put("theme.color", "#0F52FF")
                put("prefill.email", FirebaseAuth.getInstance().currentUser?.email ?: "user@jobai.com")
            }
            checkout.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(context, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    val allPlans = listOf(
        PricingPlan(
            id = "free",
            name = "Free Tier",
            price = "₹0",
            rawAmount = 0,
            level = 0,
            period = "Forever",
            subtitle = "Starter Access",
            description = "Basic AI job matching with limited monthly applications.",
            features = listOf("5–10 applications / month", "Basic AI resume matching", "Community access", "Email support"),
            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))),
            textColor = TextDark,
            cardColor = Color(0xFFFFFFFF),
            buttonColor = Color(0xFF64748B)
        ),
        PricingPlan(
            id = "starter",
            name = "Starter Pro",
            price = "₹299",
            rawAmount = 29900,
            level = 1,
            period = "per month",
            subtitle = "Active Job Seekers",
            description = "Boost response rate with AI personalized outreach emails.",
            features = listOf("~100 applications / month", "AI-personalized email outreach", "Smart resume optimization", "Priority support"),
            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))),
            textColor = Color(0xFF1E3A8A),
            cardColor = Color(0xFFEFF6FF),
            buttonColor = Color(0xFF2563EB),
            badgeText = "POPULAR"
        ),
        PricingPlan(
            id = "pro",
            name = "Pro Career ⭐",
            price = "₹699",
            rawAmount = 69900,
            level = 2,
            period = "per month",
            subtitle = "Rapid Growth",
            description = "Fully automated AI Auto-Apply to land interviews 5x faster.",
            features = listOf("~500 applications / month", "Fully automated AI Auto-Apply", "Real-time recruiter view tracking", "24/7 Priority Support"),
            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81))),
            textColor = Color(0xFFFFFFFF),
            cardColor = Color(0xFF1E1B4B),
            buttonColor = Color(0xFF0F52FF),
            isPopular = true,
            badgeText = "BEST VALUE"
        ),
        PricingPlan(
            id = "unlimited",
            name = "Unlimited VIP",
            price = "₹1,299",
            rawAmount = 129900,
            level = 3,
            period = "per month",
            subtitle = "Maximum Speed",
            description = "Uncapped applications and VIP outreach for senior professionals.",
            features = listOf("High-volume unlimited auto-apply", "Unlimited AI email outreach", "Dedicated account manager", "VIP Support"),
            backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
            textColor = Color(0xFF78350F),
            cardColor = Color(0xFFFEF3C7),
            buttonColor = Color(0xFFD97706),
            badgeText = "ELITE"
        )
    )

    val activeLevel = getPlanLevel(userActivePlan)
    val displayPlans = if (userActivePlan == "free") allPlans else allPlans.filter { it.level >= activeLevel }
    val daysLeft = if (premiumExpiry > System.currentTimeMillis()) ((premiumExpiry - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt() else 0

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
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Crown",
                            tint = AppBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Subscription & Upgrades",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (userActivePlan != "free") "Active Plan: ${userActivePlan.uppercase()} ($daysLeft days remaining). Upgrade anytime!"
                    else "Swipe through our transparent plans. Upgrade anytime securely via Razorpay.",
                    fontSize = 13.sp,
                    color = if (userActivePlan != "free") Color(0xFF10B981) else TextMuted,
                    textAlign = TextAlign.Center,
                    fontWeight = if (userActivePlan != "free") FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ============ HORIZONTAL SCROLLABLE CAROUSEL ============
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                displayPlans.forEach { plan ->
                    val isCurrentActive = plan.id == userActivePlan
                    val isUpgrade = plan.level > activeLevel

                    val diffAmountStr = if (isUpgrade && activeLevel > 0) {
                        val currentBase = when (userActivePlan) { "starter" -> 299; "pro" -> 699; else -> 0 }
                        val targetBase = when (plan.id) { "starter" -> 299; "pro" -> 699; "unlimited" -> 1299; else -> 0 }
                        val diff = targetBase - currentBase
                        "Pay only ₹$diff diff"
                    } else null

                    PricingCardCarouselItem(
                        plan = plan,
                        isCurrentActive = isCurrentActive,
                        isUpgrade = isUpgrade,
                        proratedText = diffAmountStr,
                        onSelect = { startRazorpayPayment(plan) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Trust Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("100% Secure Razorpay Checkout", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Instant activation & automatic prorated upgrade calculation.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun PricingCardCarouselItem(
    plan: PricingPlan,
    isCurrentActive: Boolean,
    isUpgrade: Boolean,
    proratedText: String?,
    onSelect: () -> Unit
) {
    val secondaryTextColor = if (plan.isPopular || plan.id == "pro") Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (plan.isPopular || plan.id == "pro") Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = Modifier
            .width(290.dp)
            .shadow(
                elevation = if (isCurrentActive) 14.dp else if (plan.isPopular) 12.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = plan.buttonColor.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = plan.cardColor,
        border = if (isCurrentActive) BorderStroke(2.5.dp, Color(0xFF10B981)) else if (plan.isPopular) BorderStroke(2.dp, Color(0xFF38BDF8)) else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(plan.backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.subtitle.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = plan.buttonColor,
                        letterSpacing = 1.sp
                    )

                    if (isCurrentActive) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "ACTIVE ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else if (plan.badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = plan.buttonColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = plan.badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = plan.buttonColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = plan.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = plan.textColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = plan.description,
                    fontSize = 12.sp,
                    color = secondaryTextColor,
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Price & Prorated Difference
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = plan.price,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = plan.textColor,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plan.period,
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    if (proratedText != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = proratedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Features
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(150.dp)
                ) {
                    plan.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (plan.isPopular || plan.id == "pro") Color(0xFF38BDF8) else Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                fontSize = 12.sp,
                                color = plan.textColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentActive) Color(0xFF10B981) else plan.buttonColor,
                        contentColor = Color.White
                    ),
                    enabled = !isCurrentActive,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = when {
                            isCurrentActive -> "Your Active Plan"
                            isUpgrade -> "Upgrade Plan"
                            else -> "Subscribe via Razorpay"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}