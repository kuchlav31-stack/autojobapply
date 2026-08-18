package com.dark.jobai.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Feed post data model
 */
data class FeedPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val userHeadline: String = "",
    val userProfileImage: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likes: Long = 0L,
    val comments: Long = 0L,
    val shares: Long = 0L,
    val createdAt: Long = 0L,
    val isLiked: Boolean = false,
    val likedBy: List<String> = emptyList()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): FeedPost {
            return FeedPost(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                userName = doc.getString("userName") ?: "Anonymous",
                userHeadline = doc.getString("userHeadline") ?: "",
                userProfileImage = doc.getString("userProfileImage") ?: "",
                content = doc.getString("content") ?: "",
                imageUrl = doc.getString("imageUrl") ?: "",
                likes = doc.getLong("likes") ?: 0L,
                comments = doc.getLong("comments") ?: 0L,
                shares = doc.getLong("shares") ?: 0L,
                createdAt = doc.getLong("createdAt") ?: 0L,
                isLiked = doc.getBoolean("isLiked") ?: false,
                likedBy = (doc.get("likedBy") as? List<String>) ?: emptyList()
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "userName" to userName,
            "userHeadline" to userHeadline,
            "userProfileImage" to userProfileImage,
            "content" to content,
            "imageUrl" to imageUrl,
            "likes" to likes,
            "comments" to comments,
            "shares" to shares,
            "createdAt" to createdAt,
            "likedBy" to likedBy
        )
    }
}