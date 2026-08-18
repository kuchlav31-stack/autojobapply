package com.dark.jobai.service

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.dark.jobai.data.model.Plan
import com.dark.jobai.data.repository.PaymentRepository
import com.dark.jobai.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpiPaymentService(
    private val activity: Activity,
    private val userRepository: UserRepository = UserRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) {

    companion object {
        private const val TAG = "UpiPayment"
        private const val UPI_REQUEST_CODE = 1001

        // Business UPI ID
        const val MERCHANT_UPI_ID = "lavkushbind61-1@okhdfcbank"
        const val MERCHANT_NAME = "JobAI"
    }

    private var currentPlan: Plan? = null
    private var currentUserId: String? = null

    fun startPayment(
        userId: String,
        plan: Plan,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentPlan = plan
        currentUserId = userId

        try {
            val amount = plan.price.toString()

            val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", MERCHANT_UPI_ID)
                .appendQueryParameter("pn", MERCHANT_NAME)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("tn", "${plan.name} Plan - JobAI")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Check if UPI app exists
            val chooser = Intent.createChooser(intent, "Pay with UPI")

            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(chooser, UPI_REQUEST_CODE)
            } else {
                onError("No UPI app found. Please install Google Pay, PhonePe, or Paytm.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "UPI payment failed", e)
            onError(e.localizedMessage ?: "Payment failed")
        }
    }

    fun handlePaymentResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (requestCode != UPI_REQUEST_CODE) return

        val userId = currentUserId ?: return
        val plan = currentPlan ?: return

        when (resultCode) {
            Activity.RESULT_OK -> {
                // Payment successful
                val response = data?.getStringExtra("response") ?: ""

                Log.d(TAG, "UPI Response: $response")

                // Check if payment successful
                if (response.lowercase().contains("success")) {
                    val paymentId = extractTxnId(response) ?: "upi_${System.currentTimeMillis()}"

                    CoroutineScope(Dispatchers.IO).launch {
                        // Save payment
                        val payment = com.dark.jobai.data.model.Payment(
                            userId = userId,
                            razorpayPaymentId = paymentId,
                            planId = plan.id,
                            planName = plan.name,
                            amount = plan.price,
                            status = "success",
                            paymentMethod = "UPI",
                            createdAt = System.currentTimeMillis()
                        )
                        paymentRepository.savePayment(payment)

                        // Activate premium
                        val success = userRepository.activatePremium(
                            userId = userId,
                            planId = plan.id,
                            planName = plan.name,
                            emailLimit = plan.emailLimit,
                            paymentId = paymentId
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            if (success) {
                                onSuccess(plan.name)
                            } else {
                                onError("Payment successful but activation failed")
                            }
                        }
                    }
                } else {
                    onError("Payment failed or cancelled")
                }
            }
            Activity.RESULT_CANCELED -> {
                onError("Payment cancelled")
            }
            else -> {
                onError("Payment failed")
            }
        }
    }

    private fun extractTxnId(response: String): String? {
        // Extract txnId from UPI response
        val patterns = listOf(
            "txnId=([^&]+)",
            "txnRef=([^&]+)",
            "Ref=([^&]+)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(response)
            if (match != null) return match.groupValues[1]
        }

        return null
    }
}