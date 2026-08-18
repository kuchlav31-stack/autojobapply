package com.dark.jobai.data.repository

import com.dark.jobai.data.model.Application
import com.dark.jobai.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// ✅ ApplicationStats - Top level data class
data class ApplicationStats(
    val total: Int = 0,
    val delivered: Int = 0,
    val opened: Int = 0,
    val clicked: Int = 0
)

class ApplicationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getApplicationsListener(
        userId: String,
        onResult: (List<Application>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_APPLICATIONS)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Failed to load applications")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val applications = mutableListOf<Application>()
                    for (doc in snapshot.documents) {
                        try {
                            applications.add(Application.fromDocument(doc))
                        } catch (e: Exception) {}
                    }
                    onResult(applications)
                }
            }
    }

    suspend fun getApplicationStats(userId: String): ApplicationStats {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_APPLICATIONS)
                .get()
                .await()

            var total = 0
            var delivered = 0
            var opened = 0
            var clicked = 0

            for (doc in snapshot.documents) {
                total++
                if ((doc.getLong("deliveredAt") ?: 0L) > 0) delivered++
                if ((doc.getLong("openedAt") ?: 0L) > 0) opened++
                if ((doc.getLong("clickedAt") ?: 0L) > 0) clicked++
            }

            ApplicationStats(
                total = total,
                delivered = delivered,
                opened = opened,
                clicked = clicked
            )
        } catch (e: Exception) {
            ApplicationStats()
        }
    }

    suspend fun updateApplicationStatus(
        userId: String,
        applicationId: String,
        status: String
    ): Boolean {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_APPLICATIONS)
                .document(applicationId)
                .update("status", status)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteApplication(
        userId: String,
        applicationId: String
    ): Boolean {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_APPLICATIONS)
                .document(applicationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}