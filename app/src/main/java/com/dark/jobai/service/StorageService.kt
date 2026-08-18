package com.dark.jobai.service

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * StorageService - Handles file uploads to Firebase Storage
 */
class StorageService {

    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "StorageService"
    }

    /**
     * Upload resume PDF
     */
    suspend fun uploadResume(
        userId: String,
        uri: Uri
    ): Result<String> {
        return try {
            val fileRef = storage.reference
                .child("resumes/$userId/${UUID.randomUUID()}.pdf")

            fileRef.putFile(uri).await()

            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Resume upload failed", e)
            Result.failure(e)
        }
    }

    /**
     * Upload profile image
     */
    suspend fun uploadProfileImage(
        userId: String,
        uri: Uri
    ): Result<String> {
        return try {
            val fileRef = storage.reference
                .child("profileImages/$userId/${UUID.randomUUID()}.jpg")

            fileRef.putFile(uri).await()

            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Profile image upload failed", e)
            Result.failure(e)
        }
    }

    /**
     * Upload feed post image
     */
    suspend fun uploadFeedImage(uri: Uri): Result<String> {
        return try {
            val fileRef = storage.reference
                .child("feedImages/${UUID.randomUUID()}.jpg")

            fileRef.putFile(uri).await()

            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Feed image upload failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete file
     */
    suspend fun deleteFile(fileUrl: String): Result<Unit> {
        return try {
            val fileRef = storage.getReferenceFromUrl(fileUrl)
            fileRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}