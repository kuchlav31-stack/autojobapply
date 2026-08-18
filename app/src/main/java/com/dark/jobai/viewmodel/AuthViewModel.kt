package com.dark.jobai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.repository.UserRepository
import com.dark.jobai.service.AuthService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel - Manages authentication state
 */
class AuthViewModel(
    private val authService: AuthService = AuthService(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // ============ State ============

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ============ Init ============

    init {
        checkAuthState()
    }

    // ============ Functions ============

    /**
     * Check if user is already logged in
     */
    fun checkAuthState() {
        _authState.value = AuthState.Loading

        val currentUser = authService.getCurrentUser()

        if (currentUser != null) {
            checkProfileStatus(currentUser.uid)
        } else {
            _authState.value = AuthState.LoggedOut
        }
    }

    /**
     * Check profile status and navigate accordingly
     */
    private fun checkProfileStatus(userId: String) {
        viewModelScope.launch {
            val profileCompleted = userRepository.isProfileCompleted(userId)

            _authState.value = if (profileCompleted) {
                AuthState.MainScreen
            } else {
                AuthState.ProfileSetup
            }
        }
    }

    /**
     * Sign up with email
     */
    fun signUpWithEmail(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authService.signUpWithEmail(email, password)

            result.onSuccess { user ->
                // Update display name
                authService.updateProfile(fullName)

                // Create initial user document
                val userData = mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "profileCompleted" to false,
                    "createdAt" to System.currentTimeMillis()
                )

                userRepository.updateUserFields(user.uid, userData)

                _authState.value = AuthState.ProfileSetup
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "Sign up failed"
            }

            _isLoading.value = false
        }
    }

    /**
     * Sign in with email
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authService.signInWithEmail(email, password)

            result.onSuccess { user ->
                checkProfileStatus(user.uid)
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "Sign in failed"
            }

            _isLoading.value = false
        }
    }

    /**
     * Sign in with Google
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authService.signInWithGoogle(idToken)

            result.onSuccess { user ->
                // Check if user document exists
                val exists = userRepository.userExists(user.uid)

                if (exists) {
                    checkProfileStatus(user.uid)
                } else {
                    // Create new user document
                    val userData = mapOf(
                        "fullName" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "profileCompleted" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                    userRepository.updateUserFields(user.uid, userData)
                    _authState.value = AuthState.ProfileSetup
                }
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "Google sign in failed"
            }

            _isLoading.value = false
        }
    }

    /**
     * Send password reset email
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authService.sendPasswordResetEmail(email)

            result.onSuccess {
                _errorMessage.value = "Password reset email sent!"
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "Failed to send reset email"
            }

            _isLoading.value = false
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        authService.signOut()
        _authState.value = AuthState.LoggedOut
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Auth state sealed class
 */
sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    object ProfileSetup : AuthState()
    object MainScreen : AuthState()
}