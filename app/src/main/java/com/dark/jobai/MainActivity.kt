package com.dark.jobai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dark.jobai.navigation.AppNavigation
import com.dark.jobai.service.UpiPaymentService
import com.dark.jobai.ui.theme.JobAITheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {

    private lateinit var upiPaymentService: UpiPaymentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UPI Payment Service
        upiPaymentService = UpiPaymentService(this)

        enableEdgeToEdge()

        setContent {
            JobAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    // ====================================================================
    // 1. RAZORPAY PAYMENT CALLBACKS (Added)
    // ====================================================================

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(
            this,
            "✅ Payment Successful! Payment ID: $razorpayPaymentID",
            Toast.LENGTH_LONG
        ).show()

        // Automatically upgrade user subscription in Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val updateData = mapOf(
                "premiumPlan" to "pro", // Defaulting to pro or you can pass dynamic plan from shared prefs
                "premiumUntil" to (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // 30 days validity
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this, "🎉 Subscription activated! AI Auto-Apply unlocked.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update plan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(
            this,
            "❌ Payment Failed: $response (Code: $code)",
            Toast.LENGTH_LONG
        ).show()
    }

    // ====================================================================
    // 2. UPI PAYMENT SERVICE CALLBACK
    // ====================================================================

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        upiPaymentService.handlePaymentResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data,
            onSuccess = { planName ->
                Toast.makeText(
                    this,
                    "✅ $planName activated successfully!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }
}