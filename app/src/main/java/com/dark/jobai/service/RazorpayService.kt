package com.dark.jobai.service

import android.app.Activity
import android.util.Log
import com.dark.jobai.data.model.Payment
import com.dark.jobai.data.model.Plan
import com.dark.jobai.data.repository.PaymentRepository
import com.dark.jobai.data.repository.UserRepository
import com.dark.jobai.util.Constants
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RazorpayService(
    private val activity: Activity,
    private val userRepository: UserRepository = UserRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : PaymentResultWithDataListener {

    companion object {
        private const val TAG = "RazorpayService"
    }

    private var currentPlan: Plan? = null
    private var currentUserId: String? = null
    private var onPaymentSuccessCallback: ((String, String) -> Unit)? = null
    private var onPaymentErrorCallback: ((String) -> Unit)? = null

    fun startPayment(
        userId: String,
        plan: Plan,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentPlan = plan
        currentUserId = userId
        onPaymentSuccessCallback = onSuccess
        onPaymentErrorCallback = onError

        Log.d(TAG, "💳 Starting payment for ${plan.name}")
        Log.d(TAG, "Amount: ₹${plan.price}")
        Log.d(TAG, "Key: ${Constants.RAZORPAY_KEY_ID.take(10)}...")

        try {
            // Preload checkout
            Checkout.preload(activity.applicationContext)

            val checkout = Checkout()
            checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

            val options = JSONObject().apply {
                put("name", "JobAI")
                put("description", "${plan.name} Plan")
                put("currency", "INR")
                put("amount", plan.price * 100)
                put("send_sms_hash", true)

                put("notes", JSONObject().apply {
                    put("userId", userId)
                    put("planId", plan.id)
                    put("planName", plan.name)
                })

                put("theme", JSONObject().apply {
                    put("color", "#00E676")
                })
            }

            checkout.open(activity, options)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Payment start failed", e)
            onError(e.localizedMessage ?: "Payment failed to start")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Log.d(TAG, "✅ Payment Success: $razorpayPaymentId")

        val userId = currentUserId
        val plan = currentPlan

        if (userId == null || plan == null) {
            onPaymentErrorCallback?.invoke("Payment data missing")
            return
        }

        // Process in background - avoid main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Save payment
                val payment = Payment(
                    userId = userId,
                    razorpayPaymentId = razorpayPaymentId ?: "",
                    razorpayOrderId = paymentData?.orderId ?: "",
                    planId = plan.id,
                    planName = plan.name,
                    amount = plan.price,
                    status = "success",
                    createdAt = System.currentTimeMillis()
                )

                val saved = paymentRepository.savePayment(payment)
                Log.d(TAG, "Payment saved: $saved")

                // Activate premium
                val activated = userRepository.activatePremium(
                    userId = userId,
                    planId = plan.id,
                    planName = plan.name,
                    emailLimit = plan.emailLimit,
                    paymentId = razorpayPaymentId ?: ""
                )

                Log.d(TAG, "Premium activated: $activated")

                // Callback on main thread
                withContext(Dispatchers.Main) {
                    if (activated) {
                        onPaymentSuccessCallback?.invoke(
                            razorpayPaymentId ?: "",
                            plan.name
                        )
                    } else {
                        onPaymentErrorCallback?.invoke("Payment successful but activation failed. Contact support.")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Processing error", e)
                withContext(Dispatchers.Main) {
                    onPaymentErrorCallback?.invoke("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Log.e(TAG, "❌ Payment Error: $code - $response")

        val message = when (code) {
            Checkout.NETWORK_ERROR -> "Network error. Check your internet."
            Checkout.INVALID_OPTIONS -> "Invalid payment options."
            Checkout.PAYMENT_CANCELED -> "Payment cancelled. No money deducted."
            else -> response ?: "Payment failed. Please try again."
        }

        CoroutineScope(Dispatchers.Main).launch {
            onPaymentErrorCallback?.invoke(message)
        }
    }
}