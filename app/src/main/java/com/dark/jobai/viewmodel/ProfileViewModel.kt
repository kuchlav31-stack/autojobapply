package com.dark.jobai.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.model.User
import com.dark.jobai.data.repository.UserRepository
import com.dark.jobai.service.PdfExtractorService
import com.dark.jobai.service.StorageService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel - Manages user profile
 */
class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val storageService: StorageService = StorageService(),
    private val pdfExtractorService: PdfExtractorService = PdfExtractorService()
) : ViewModel() {

    // ============ State ============

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // ============ Init ============

    init {
        loadProfile()
    }

    // ============ Functions ============

    /**
     * Load user profile
     */
    fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return

        userRepository.getUserListener(
            userId = userId,
            onResult = { user ->
                _user.value = user
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    /**
     * Save profile
     */
    fun saveProfile(user: User) {
        viewModelScope.launch {
            _isSaving.value = true

            val success = userRepository.saveUserProfile(user)

            if (success) {
                _successMessage.value = "Profile saved successfully!"
            } else {
                _errorMessage.value = "Failed to save profile"
            }

            _isSaving.value = false
        }
    }

    /**
     * Upload resume and extract info
     */
    fun uploadResume(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _isSaving.value = true

            val userId = auth.currentUser?.uid ?: return@launch

            // Extract text from PDF
            val resumeInfo = pdfExtractorService.extractAllInfo(context, uri)

            // Upload PDF
            val uploadResult = storageService.uploadResume(userId, uri)

            uploadResult.onSuccess { resumeUrl ->
                // Update user with resume info
                val updates = mapOf(
                    "resumeUrl" to resumeUrl,
                    "resumeText" to resumeInfo.fullText.take(5000),
                    "extractedSkills" to resumeInfo.skills,
                    "fullName" to if (resumeInfo.name.isNotEmpty()) resumeInfo.name else (_user.value?.fullName ?: ""),
                    "email" to if (resumeInfo.email.isNotEmpty()) resumeInfo.email else (_user.value?.email ?: ""),
                    "phone" to if (resumeInfo.phone.isNotEmpty()) resumeInfo.phone else (_user.value?.phone ?: ""),
                    "location" to if (resumeInfo.location.isNotEmpty()) resumeInfo.location else (_user.value?.location ?: ""),
                    "profileCompleted" to true
                )

                userRepository.updateUserFields(userId, updates)
                _successMessage.value = "Resume uploaded & profile updated!"
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "Upload failed"
            }

            _isSaving.value = false
        }
    }

    /**
     * Upload profile image
     */
    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            val result = storageService.uploadProfileImage(userId, uri)

            result.onSuccess { imageUrl ->
                userRepository.updateUserField(userId, "profileImageUrl", imageUrl)
                _successMessage.value = "Profile image updated!"
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}