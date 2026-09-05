package com.dark.autojobapply

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import kotlinx.coroutines.tasks.await

class RazorpayManager(
    private val activity: Activity,
    private val context: Context
) : PaymentResultListener {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val RAZORPAY_KEY_ID = "rzp_live_6vd9RApruseTAi"

        // Plan IDs
        const val PLAN_PREMIUM = "premium_monthly"
        const val PLAN_PRO = "pro_monthly"
        const val PLAN_AGENCY = "agency_monthly"

        // Static references to delegate results from MainActivity
        private var activeInstance: RazorpayManager? = null

        fun onPaymentSuccess(razorpayPaymentId: String?) {
            activeInstance?.handleSuccessResult(razorpayPaymentId)
        }

        fun onPaymentError(code: Int, response: String?) {
            activeInstance?.handleErrorResult(code, response)
        }
    }

    init {
        activeInstance = this
    }

    /**
     * Start payment for selected plan
     */
    fun startPayment(
        planId: String,
        planName: String,
        amount: Int,  // Amount in rupees
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(RAZORPAY_KEY_ID)

            val userId = auth.currentUser?.uid ?: run {
                onError("User not authenticated")
                return
            }

            val options = JSONObject().apply {
                put("name", "AutoJobApply")
                put("description", "$planName Plan")
                put("currency", "INR")
                put("amount", amount * 100)  // Corrected: Amount converted from Rupees to Paise (Amount * 100)
                put("prefill", JSONObject().apply {
                    put("email", auth.currentUser?.email ?: "")
                    put("contact", "")
                })
                put("notes", JSONObject().apply {
                    put("userId", userId)
                    put("planId", planId)
                    put("planName", planName)
                })
                put("theme", JSONObject().apply {
                    put("color", "#0A66C2") // Matched with LinkedIn Blue Theme
                })
            }

            checkout.open(activity, options)

        } catch (e: Exception) {
            Log.e("Razorpay", "Payment error", e)
            onError(e.localizedMessage ?: "Payment failed")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        handleSuccessResult(razorpayPaymentId)
    }

    override fun onPaymentError(code: Int, response: String?) {
        handleErrorResult(code, response)
    }

    // Success Handler (Internal/External safe entry)
    private fun handleSuccessResult(razorpayPaymentId: String?) {
        Log.d("Razorpay", "Payment Success: $razorpayPaymentId")

        val userId = auth.currentUser?.uid
        if (userId != null && razorpayPaymentId != null) {
            // Activate premium in Firestore
            activatePremium(userId, razorpayPaymentId)
        }

        Toast.makeText(context, "✅ Payment Successful!", Toast.LENGTH_LONG).show()
    }

    // Error Handler (Internal/External safe entry)
    private fun handleErrorResult(code: Int, response: String?) {
        Log.e("Razorpay", "Payment Error: $code - $response")
        val message = when (code) {
            Checkout.NETWORK_ERROR -> "Network error. Check your internet connection."
            Checkout.PAYMENT_CANCELED -> "Payment cancelled."
            else -> response ?: "Payment failed."
        }
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }

    /**
     * Activate premium in Firestore
     */
    private fun activatePremium(userId: String, paymentId: String) {
        val planId = "premium_monthly"  // Default

        // Calculate expiry (30 days)
        val expiryDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)

        val premiumData = mapOf(
            "isPremium" to true,
            "premiumPlan" to planId,
            "premiumSince" to System.currentTimeMillis(),
            "premiumExpiry" to expiryDate,
            "emailLimit" to 50,
            "emailsSent" to 0,
            "lastPaymentId" to paymentId,
            "lastPaymentAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .set(premiumData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Razorpay", "Premium activated for: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("Razorpay", "Failed to activate premium", e)
            }
    }

    /**
     * Check if user is premium
     */
    suspend fun isPremium(userId: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getBoolean("isPremium") ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check email limit
     */
    suspend fun canSendEmail(userId: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val isPremium = doc.getBoolean("isPremium") ?: false
            val emailLimit = doc.getLong("emailLimit") ?: 0L
            val emailsSent = doc.getLong("emailsSent") ?: 0L

            isPremium && emailsSent < emailLimit
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Increment email count
     */
    suspend fun incrementEmailCount(userId: String) {
        try {
            db.collection("users").document(userId)
                .update("emailsSent", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Log.e("Razorpay", "Failed to increment email count", e)
        }
    }
}