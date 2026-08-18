package com.dark.jobai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.model.Plan
import com.dark.jobai.data.model.User
import com.dark.jobai.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PricingViewModel - Manages premium plans and subscription
 */
class PricingViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // ============ State ============

    private val _plans = MutableStateFlow<List<Plan>>(Plan.getAllPlans())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // ============ Init ============

    init {
        loadUserStatus()
    }

    // ============ Functions ============

    /**
     * Load user premium status
     */
    fun loadUserStatus() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _currentUser.value = user
            _isPremium.value = user?.isPremiumActive() ?: false
            _isLoading.value = false
        }
    }

    /**
     * Get selected plan
     */
    fun getPlanById(planId: String): Plan? {
        return _plans.value.find { it.id == planId }
    }

    /**
     * Check if user can send email
     */
    fun canSendEmail(): Boolean {
        return _currentUser.value?.canSendEmail() ?: false
    }

    /**
     * Get remaining emails
     */
    fun getRemainingEmails(): Long {
        return _currentUser.value?.getRemainingEmails() ?: 0L
    }

    /**
     * Clear error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}