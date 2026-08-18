package com.dark.jobai.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Payment record data model
 */
data class Payment(
    val id: String = "",
    val userId: String = "",
    val razorpayPaymentId: String = "",
    val razorpayOrderId: String = "",
    val planId: String = "",
    val planName: String = "",
    val amount: Int = 0,
    val currency: String = "INR",
    val status: String = "pending",  // pending, success, failed
    val paymentMethod: String = "",
    val createdAt: Long = 0L
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Payment {
            return Payment(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                razorpayPaymentId = doc.getString("razorpayPaymentId") ?: "",
                razorpayOrderId = doc.getString("razorpayOrderId") ?: "",
                planId = doc.getString("planId") ?: "",
                planName = doc.getString("planName") ?: "",
                amount = (doc.getLong("amount") ?: 0L).toInt(),
                currency = doc.getString("currency") ?: "INR",
                status = doc.getString("status") ?: "pending",
                paymentMethod = doc.getString("paymentMethod") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "razorpayPaymentId" to razorpayPaymentId,
            "razorpayOrderId" to razorpayOrderId,
            "planId" to planId,
            "planName" to planName,
            "amount" to amount,
            "currency" to currency,
            "status" to status,
            "paymentMethod" to paymentMethod,
            "createdAt" to createdAt
        )
    }
}