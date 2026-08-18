package com.dark.jobai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.jobai.data.model.Job
import com.dark.jobai.data.model.User
import com.dark.jobai.data.repository.JobRepository
import com.dark.jobai.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobsViewModel(
    private val jobRepository: JobRepository = JobRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())
    val allJobs: StateFlow<List<Job>> = _allJobs.asStateFlow()

    private val _filteredJobs = MutableStateFlow<List<Job>>(emptyList())
    val filteredJobs: StateFlow<List<Job>> = _filteredJobs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _workTypeFilter = MutableStateFlow("All")
    val workTypeFilter: StateFlow<String> = _workTypeFilter.asStateFlow()

    private val _showEmailOnly = MutableStateFlow(false)
    val showEmailOnly: StateFlow<Boolean> = _showEmailOnly.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<String>>(emptySet())
    val savedJobIds: StateFlow<Set<String>> = _savedJobIds.asStateFlow()

    private var currentUser: User? = null
    private val auth = FirebaseAuth.getInstance()

    init {
        loadJobs()
        loadSavedJobs()
    }

    /**
     * Load jobs from Firestore
     */
    fun loadJobs() {
        _isLoading.value = true
        _errorMessage.value = null

        val userId = auth.currentUser?.uid

        viewModelScope.launch {
            // Load user profile for smart matching
            if (userId != null) {
                currentUser = userRepository.getUserById(userId)
            }
        }

        jobRepository.getJobsListener(
            onResult = { jobs ->
                _allJobs.value = jobs
                applyFilters()
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    /**
     * Load saved job IDs
     */
    fun loadSavedJobs() {
        val userId = auth.currentUser?.uid ?: return

        jobRepository.getSavedJobsListener(
            userId = userId,
            onResult = { jobs ->
                _savedJobIds.value = jobs.map { it.id }.toSet()
            },
            onError = { }
        )
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Update work type filter
     */
    fun updateWorkTypeFilter(filter: String) {
        _workTypeFilter.value = filter
        applyFilters()
    }

    /**
     * Toggle email only filter
     */
    fun toggleEmailOnly(enabled: Boolean) {
        _showEmailOnly.value = enabled
        applyFilters()
    }

    /**
     * Apply all filters with smart matching
     */
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val workType = _workTypeFilter.value
        val emailOnly = _showEmailOnly.value
        val user = currentUser

        var jobs = _allJobs.value

        // Basic filters
        jobs = jobs.filter { job ->
            val matchesSearch = query.isBlank() ||
                    job.title.lowercase().contains(query) ||
                    job.company.lowercase().contains(query) ||
                    job.location.lowercase().contains(query)

            val matchesWorkType = workType == "All" || job.workType == workType
            val matchesEmail = !emailOnly || job.hasEmail

            matchesSearch && matchesWorkType && matchesEmail
        }

        // Smart matching based on user profile
        if (user != null && user.profileCompleted) {
            jobs = jobs.map { job ->
                val score = calculateMatchScore(user, job)
                job.copy(matchScore = score)
            }

            // Sort by match score
            jobs = jobs.sortedWith(
                compareByDescending<Job> { it.matchScore }
                    .thenByDescending { it.postedAt }
            )
        } else {
            // No user profile - sort by email and date
            jobs = jobs.sortedWith(
                compareByDescending<Job> { it.hasEmail }
                    .thenByDescending { it.postedAt }
            )
        }

        _filteredJobs.value = jobs
    }

    /**
     * Calculate match score between user and job
     */
    private fun calculateMatchScore(user: User, job: Job): Int {
        var score = 0

        // Skills matching (40%)
        val userSkills = user.skills.map { it.lowercase() }
        val jobText = "${job.title} ${job.description} ${job.tags.joinToString(" ")}".lowercase()

        var matchedSkills = 0
        for (skill in userSkills) {
            if (jobText.contains(skill)) {
                matchedSkills++
            }
        }

        if (userSkills.isNotEmpty()) {
            score += (matchedSkills * 40) / userSkills.size
        }

        // Role matching (25%)
        val preferredRole = user.preferredRole.lowercase()
        val jobTitle = job.title.lowercase()

        if (preferredRole.isNotEmpty() && jobTitle.contains(preferredRole)) {
            score += 25
        }

        // Work type matching (15%)
        if (user.workPreference == job.workType) {
            score += 15
        } else if (user.jobType == "All") {
            score += 10
        }

        // Location matching (10%)
        if (job.workType == "Remote") {
            score += 10
        } else {
            val userLocation = user.location.lowercase()
            val jobLocation = job.location.lowercase()
            if (userLocation.isNotEmpty() && jobLocation.contains(userLocation)) {
                score += 10
            }
        }

        // Experience matching (10%)
        val expYears = user.experienceYears.toIntOrNull()
        if (expYears != null) {
            val jobExp = job.experienceRequired.lowercase()
            if (jobExp == "not disclosed" || jobExp.contains("$expYears")) {
                score += 10
            }
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Save job
     */
    fun saveJob(job: Job) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val success = jobRepository.saveJob(userId, job)
            if (success) {
                val updated = _savedJobIds.value.toMutableSet()
                updated.add(job.id)
                _savedJobIds.value = updated
            }
        }
    }

    /**
     * Remove saved job
     */
    fun removeSavedJob(jobId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val success = jobRepository.removeSavedJob(userId, jobId)
            if (success) {
                val updated = _savedJobIds.value.toMutableSet()
                updated.remove(jobId)
                _savedJobIds.value = updated
            }
        }
    }

    /**
     * Check if job is saved
     */
    fun isJobSaved(jobId: String): Boolean {
        return jobId in _savedJobIds.value
    }

    /**
     * Clear error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}