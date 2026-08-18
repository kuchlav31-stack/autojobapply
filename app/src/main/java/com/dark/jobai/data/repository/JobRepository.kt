package com.dark.jobai.data.repository

import com.dark.jobai.data.model.Job
import com.dark.jobai.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class JobRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val jobsCollection = firestore.collection(Constants.COLLECTION_JOBS)

    /**
     * Get all jobs - Simple query (No composite index needed)
     */
    fun getJobsListener(
        limit: Long = 200,
        onResult: (List<Job>) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ सिर्फ postedAt से order करो - कोई whereEqualTo नहीं
        jobsCollection
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Failed to load jobs")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val jobs = mutableListOf<Job>()
                    for (doc in snapshot.documents) {
                        try {
                            val job = Job.fromDocument(doc)
                            // Client-side filter for active jobs
                            if (job.isActive) {
                                jobs.add(job)
                            }
                        } catch (e: Exception) {}
                    }

                    // Sort: Email jobs first, then latest
                    jobs.sortWith(
                        compareByDescending<Job> { it.hasEmail }
                            .thenByDescending { it.postedAt }
                    )

                    onResult(jobs)
                }
            }
    }

    /**
     * Get job by ID
     */
    suspend fun getJobById(jobId: String): Job? {
        return try {
            val doc = jobsCollection.document(jobId).get().await()
            if (doc.exists()) Job.fromDocument(doc) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Filter jobs by work type - Simple query
     */
    suspend fun filterJobsByWorkType(workType: String, limit: Long = 100): List<Job> {
        return try {
            val snapshot = jobsCollection
                .whereEqualTo("workType", workType)
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try { Job.fromDocument(doc) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get jobs with email - Simple query
     */
    suspend fun getJobsWithEmail(limit: Long = 100): List<Job> {
        return try {
            val snapshot = jobsCollection
                .whereEqualTo("hasEmail", true)
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try { Job.fromDocument(doc) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save job for user
     */
    suspend fun saveJob(userId: String, job: Job): Boolean {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_SAVED_JOBS)
                .document(job.id)
                .set(job.toMap())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Remove saved job
     */
    suspend fun removeSavedJob(userId: String, jobId: String): Boolean {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_SAVED_JOBS)
                .document(jobId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get saved jobs for user - Simple listener
     */
    fun getSavedJobsListener(
        userId: String,
        onResult: (List<Job>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_SAVED_JOBS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Failed to load saved jobs")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val jobs = mutableListOf<Job>()
                    for (doc in snapshot.documents) {
                        try {
                            jobs.add(Job.fromDocument(doc))
                        } catch (e: Exception) {}
                    }

                    // Client-side sorting
                    jobs.sortByDescending { it.postedAt }

                    onResult(jobs)
                }
            }
    }

    /**
     * Check if job is saved
     */
    suspend fun isJobSaved(userId: String, jobId: String): Boolean {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_SAVED_JOBS)
                .document(jobId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get job count - Simple count
     */
    suspend fun getJobCount(): Long {
        return try {
            jobsCollection.get().await().size().toLong()
        } catch (e: Exception) {
            0L
        }
    }
}