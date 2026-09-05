package com.dark.jobai.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.model.FeedPost
import com.dark.jobai.data.repository.FeedRepository
import com.dark.jobai.data.repository.UserRepository
import com.dark.jobai.service.StorageService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FeedViewModel - Manages community feed
 */
class FeedViewModel(
    private val feedRepository: FeedRepository = FeedRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val storageService: StorageService = StorageService()
) : ViewModel() {

    // ============ State ============

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isCreatingPost = MutableStateFlow(false)
    val isCreatingPost: StateFlow<Boolean> = _isCreatingPost.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // ============ Init ============

    init {
        loadPosts()
    }

    // ============ Functions ============

    /**
     * Load feed posts
     */
    fun loadPosts() {
        val currentUserId = auth.currentUser?.uid
        feedRepository.getFeedPostsListener(
            currentUserId = currentUserId,
            onResult = { posts ->
                _posts.value = posts
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    /**
     * Create new post
     */
    fun createPost(content: String, imageUri: Uri?, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isCreatingPost.value = true

            val userId = auth.currentUser?.uid ?: run {
                _errorMessage.value = "User not logged in"
                _isCreatingPost.value = false
                onComplete(false)
                return@launch
            }

            try {
                // Upload image if provided
                var imageUrl = ""
                if (imageUri != null) {
                    val result = storageService.uploadFeedImage(imageUri)
                    imageUrl = result.getOrNull() ?: ""
                }

                // Get user info directly from Firestore
                val user = userRepository.getUserById(userId)

                // Create post
                val post = FeedPost(
                    userId = userId,
                    userName = if (!user?.fullName.isNullOrBlank()) user!!.fullName else "JobAI User",
                    userHeadline = user?.headline ?: "Member",
                    userProfileImage = user?.profileImageUrl ?: "",
                    content = content,
                    imageUrl = imageUrl,
                    createdAt = System.currentTimeMillis(),
                    likedBy = emptyList()
                )

                val success = feedRepository.createPost(post)

                if (success) {
                    loadPosts()
                    onComplete(true)
                } else {
                    android.util.Log.e("FeedViewModel", "Failed to create post: Firestore write returned false")
                    _errorMessage.value = "Failed to create post"
                    onComplete(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "Exception creating post", e)
                _errorMessage.value = e.localizedMessage
                onComplete(false)
            }

            _isCreatingPost.value = false
        }
    }

    /**
     * Like post
     */
    fun likePost(post: FeedPost) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            if (post.isLiked) {
                feedRepository.unlikePost(post.id, userId)
            } else {
                feedRepository.likePost(post.id, userId)
            }
        }
    }

    /**
     * Delete post
     */
    fun deletePost(postId: String) {
        viewModelScope.launch {
            feedRepository.deletePost(postId)
            loadPosts()
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}