package com.dark.jobai.data.repository

import com.dark.jobai.data.model.User
import com.dark.jobai.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * UserRepository - Handles all user-related Firestore operations
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection(Constants.COLLECTION_USERS)

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) User.fromDocument(doc) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get user with real-time listener
     */
    fun getUserListener(
        userId: String,
        onResult: (User?) -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Failed to load user")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    onResult(User.fromDocument(snapshot))
                } else {
                    onResult(null)
                }
            }
    }

    /**
     * Create or update user profile
     */
    suspend fun saveUserProfile(user: User): Boolean {
        return try {
            usersCollection.document(user.uid)
                .set(user.toMap(), com.google.firebase.firestore.SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update specific fields
     */
    suspend fun updateUserField(
        userId: String,
        field: String,
        value: Any
    ): Boolean {
        return try {
            usersCollection.document(userId)
                .update(field, value)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update multiple fields
     */
    suspend fun updateUserFields(
        userId: String,
        fields: Map<String, Any>
    ): Boolean {
        return try {
            usersCollection.document(userId)
                .update(fields)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if user exists
     */
    suspend fun userExists(userId: String): Boolean {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if profile is completed
     */
    suspend fun isProfileCompleted(userId: String): Boolean {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                doc.getBoolean("profileCompleted") ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update email count
     */
    suspend fun incrementEmailCount(userId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("emailsSent", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Activate premium
     */
    suspend fun activatePremium(
        userId: String,
        planId: String,
        planName: String,
        emailLimit: Long,
        paymentId: String
    ): Boolean {
        return try {
            val premiumData = mapOf(
                "isPremium" to true,
                "premiumPlan" to planId,
                "premiumPlanName" to planName,
                "premiumSince" to System.currentTimeMillis(),
                "premiumExpiry" to (System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)),
                "emailLimit" to emailLimit,
                "emailsSent" to 0L,
                "lastPaymentId" to paymentId,
                "lastPaymentAt" to System.currentTimeMillis()
            )

            usersCollection.document(userId)
                .set(premiumData, com.google.firebase.firestore.SetOptions.merge())
                .await()

            android.util.Log.d("UserRepository", "Premium activated for: $userId")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to activate premium", e)
            false
        }
    }

    /**
     * Save email template
     */
    suspend fun saveEmailTemplate(
        userId: String,
        subjectTemplate: String,
        bodyTemplate: String,
        autoSend: Boolean
    ): Boolean {
        return try {
            val templateData = mapOf(
                "emailSubjectTemplate" to subjectTemplate,
                "emailBodyTemplate" to bodyTemplate,
                "emailAutoSend" to autoSend
            )

            usersCollection.document(userId)
                .set(templateData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete user account
     */
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            usersCollection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}