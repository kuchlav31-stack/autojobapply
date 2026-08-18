package com.dark.jobai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.model.Application
import com.dark.jobai.data.repository.ApplicationRepository
import com.dark.jobai.data.repository.ApplicationStats
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ApplicationsViewModel - Manages application tracking
 */
class ApplicationsViewModel(
    private val applicationRepository: ApplicationRepository = ApplicationRepository()
) : ViewModel() {

    // ============ State ============

    private val _applications = MutableStateFlow<List<Application>>(emptyList())
    val applications: StateFlow<List<Application>> = _applications.asStateFlow()

    private val _stats = MutableStateFlow(ApplicationStats())
    val stats: StateFlow<ApplicationStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // ============ Init ============

    init {
        loadApplications()
    }

    // ============ Functions ============

    /**
     * Load applications
     */
    fun loadApplications() {
        val userId = auth.currentUser?.uid ?: return

        applicationRepository.getApplicationsListener(
            userId = userId,
            onResult = { applications ->
                _applications.value = applications
                _isLoading.value = false
                loadStats()
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    /**
     * Load application statistics
     */
    private fun loadStats() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _stats.value = applicationRepository.getApplicationStats(userId)
        }
    }

    /**
     * Update application status
     */
    fun updateStatus(applicationId: String, status: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            applicationRepository.updateApplicationStatus(userId, applicationId, status)
            loadApplications()
        }
    }

    /**
     * Delete application
     */
    fun deleteApplication(applicationId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            applicationRepository.deleteApplication(userId, applicationId)
            loadApplications()
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}