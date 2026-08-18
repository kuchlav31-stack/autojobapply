package com.dark.jobai.data.repository

import android.util.Log
import com.dark.jobai.data.model.Payment
import com.dark.jobai.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "PaymentRepository"
    }

    private val paymentsCollection = firestore.collection(Constants.COLLECTION_PAYMENTS)

    suspend fun savePayment(payment: Payment): Boolean {
        return try {
            val docRef = paymentsCollection.document(payment.razorpayPaymentId.ifEmpty {
                "payment_${System.currentTimeMillis()}"
            })

            docRef.set(payment.toMap()).await()
            Log.d(TAG, "Payment saved: ${payment.razorpayPaymentId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save payment", e)
            false
        }
    }
}