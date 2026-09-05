package com.dark.jobai.data.repository

import com.dark.jobai.data.model.FeedPost
import com.dark.jobai.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * FeedRepository - Handles community feed operations
 */
class FeedRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val feedCollection = firestore.collection(Constants.COLLECTION_FEED)

    /**
     * Get feed posts with real-time listener
     */
    fun getFeedPostsListener(
        limit: Long = 100,
        currentUserId: String? = null,
        onResult: (List<FeedPost>) -> Unit,
        onError: (String) -> Unit
    ) {
        feedCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Failed to load feed")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = mutableListOf<FeedPost>()
                    for (doc in snapshot.documents) {
                        try {
                            val post = FeedPost.fromDocument(doc)
                            // Set isLiked status for UI
                            posts.add(post.copy(isLiked = currentUserId != null && post.likedBy.contains(currentUserId)))
                        } catch (e: Exception) {}
                    }
                    onResult(posts)
                }
            }
    }

    /**
     * Create new post
     */
    suspend fun createPost(post: FeedPost): Boolean {
        return try {
            android.util.Log.d("FeedRepository", "Attempting to add post to collection: ${Constants.COLLECTION_FEED}")
            val result = feedCollection.add(post.toMap()).await()
            android.util.Log.d("FeedRepository", "Post created successfully with ID: ${result.id}")
            true
        } catch (e: Exception) {
            android.util.Log.e("FeedRepository", "Error creating post in Firestore", e)
            throw e // Throwing to catch specific error in ViewModel
        }
    }

    /**
     * Like post
     */
    suspend fun likePost(postId: String, userId: String): Boolean {
        return try {
            feedCollection.document(postId)
                .update(
                    "likes", com.google.firebase.firestore.FieldValue.increment(1),
                    "likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId)
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Unlike post
     */
    suspend fun unlikePost(postId: String, userId: String): Boolean {
        return try {
            feedCollection.document(postId)
                .update(
                    "likes", com.google.firebase.firestore.FieldValue.increment(-1),
                    "likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete post
     */
    suspend fun deletePost(postId: String): Boolean {
        return try {
            feedCollection.document(postId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get post by ID
     */
    suspend fun getPostById(postId: String): FeedPost? {
        return try {
            val doc = feedCollection.document(postId).get().await()
            if (doc.exists()) FeedPost.fromDocument(doc) else null
        } catch (e: Exception) {
            null
        }
    }
}