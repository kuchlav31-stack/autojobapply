package com.dark.jobai.ui.screens.main.pricing

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dark.jobai.data.model.Plan
import com.dark.jobai.service.UpiPaymentService
import com.dark.jobai.ui.theme.*
import com.dark.jobai.viewmodel.PricingViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun PricingScreen(
    onUpgradeClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val pricingViewModel: PricingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scope = rememberCoroutineScope()

    val plans by pricingViewModel.plans.collectAsState()
    val currentUser by pricingViewModel.currentUser.collectAsState()
    val isLoading by pricingViewModel.isLoading.collectAsState()

    var isProcessingPayment by remember { mutableStateOf(false) }

    fun handlePayment(plan: Plan) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // ✅ Activity null check
        if (activity == null) {
            Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessingPayment = true

        val upiService = UpiPaymentService(activity)

        upiService.startPayment(
            userId = userId,
            plan = plan,
            onSuccess = { planName ->
                isProcessingPayment = false
                Toast.makeText(context, "✅ $planName activated!", Toast.LENGTH_LONG).show()
                scope.launch { pricingViewModel.loadUserStatus() }
            },
            onError = { error ->
                isProcessingPayment = false
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Text(
            "Premium Plans",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

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
                if (currentUser?.isPremiumActive() == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A2A)),
                        border = BorderStroke(2.dp, GoldPremium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("✅ Premium Active", color = GoldPremium, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Plan: ${(currentUser?.premiumPlan ?: "Free").replace("_", " ").uppercase()}",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Choose Your Plan", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                plans.forEach { plan ->
                    val isCurrentPlan = plan.id == currentUser?.premiumPlan

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentPlan) Color(0xFF1A3A2A) else SurfaceDark
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isCurrentPlan) GoldPremium else BorderGray.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(plan.name, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(plan.description, color = TextGray, fontSize = 12.sp)
                                }
                                Text("₹${plan.price}", color = PrimaryGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            plan.features.forEach { feature ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feature, color = TextWhite.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { if (!isCurrentPlan) handlePayment(plan) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = !isCurrentPlan && !isProcessingPayment,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrentPlan) GoldPremium.copy(alpha = 0.3f) else PrimaryGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isProcessingPayment) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        if (isCurrentPlan) "Current Plan" else "Pay ₹${plan.price} via UPI",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}