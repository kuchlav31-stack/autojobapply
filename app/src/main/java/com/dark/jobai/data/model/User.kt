package com.dark.jobai.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val uid: String = "",
    val fullName: String = "",
    val headline: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: String = "",
    val preferredRole: String = "",
    val currentCompany: String = "",          // NEW: Master Profile Field
    val highestDegree: String = "",           // NEW: Master Profile Field
    val workPreference: String = "Remote",
    val jobType: String = "All",
    val preferredLocations: List<String> = emptyList(),
    val salaryRange: String = "",
    val resumeUrl: String = "",
    val resumeText: String = "",
    val profileImageUrl: String = "",
    val linkedinUrl: String = "",
    val githubUrl: String = "",
    val portfolioUrl: String = "",
    val twitterUrl: String = "",
    val currentCtc: String = "",              // NEW: Master Profile Field
    val expectedCtc: String = "",             // NEW: Master Profile Field
    val noticePeriod: String = "",
    val profileCompleted: Boolean = false,
    val isPremium: Boolean = false,
    val premiumPlan: String = "free",
    val premiumExpiry: Long = 0L,
    val emailLimit: Long = 0L,
    val emailsSent: Long = 0L,
    val emailSubjectTemplate: String = "",
    val emailBodyTemplate: String = "",
    val emailAutoSend: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val autoApplyEnabled: Boolean = false,
    val lastAutoApplyAt: Long = 0L,
    val lastAutoApplyCount: Long = 0L
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): User {
            return User(
                uid = doc.id,
                fullName = doc.getString("fullName") ?: "",
                headline = doc.getString("headline") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                location = doc.getString("location") ?: "",
                bio = doc.getString("bio") ?: "",
                skills = (doc.get("skills") as? List<String>) ?: emptyList(),
                experienceYears = doc.getString("experienceYears") ?: "",
                preferredRole = doc.getString("preferredRole") ?: "",
                currentCompany = doc.getString("currentCompany") ?: "",
                highestDegree = doc.getString("highestDegree") ?: "",
                workPreference = doc.getString("workPreference") ?: "Remote",
                jobType = doc.getString("jobType") ?: "All",
                preferredLocations = (doc.get("preferredLocations") as? List<String>) ?: emptyList(),
                salaryRange = doc.getString("salaryRange") ?: "",
                resumeUrl = doc.getString("resumeUrl") ?: "",
                resumeText = doc.getString("resumeText") ?: "",
                profileImageUrl = doc.getString("profileImageUrl") ?: "",
                linkedinUrl = doc.getString("linkedinUrl") ?: "",
                githubUrl = doc.getString("githubUrl") ?: "",
                portfolioUrl = doc.getString("portfolioUrl") ?: "",
                twitterUrl = doc.getString("twitterUrl") ?: "",
                currentCtc = doc.getString("currentCtc") ?: doc.getString("currentSalary") ?: "",
                expectedCtc = doc.getString("expectedCtc") ?: doc.getString("expectedSalary") ?: "",
                noticePeriod = doc.getString("noticePeriod") ?: "",
                profileCompleted = doc.getBoolean("profileCompleted") ?: doc.getBoolean("isProfileCompleted") ?: false,
                isPremium = doc.getBoolean("isPremium") ?: (doc.getString("premiumPlan") != null && doc.getString("premiumPlan") != "free"),
                premiumPlan = doc.getString("premiumPlan") ?: "free",
                premiumExpiry = doc.getLong("premiumExpiry") ?: doc.getLong("premiumUntil") ?: 0L,
                emailLimit = doc.getLong("emailLimit") ?: 0L,
                emailsSent = doc.getLong("emailsSent") ?: 0L,
                emailSubjectTemplate = doc.getString("emailSubjectTemplate") ?: "",
                emailBodyTemplate = doc.getString("emailBodyTemplate") ?: "",
                emailAutoSend = doc.getBoolean("emailAutoSend") ?: false,
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                autoApplyEnabled = doc.getBoolean("autoApplyEnabled") ?: false,
                lastAutoApplyAt = doc.getLong("lastAutoApplyAt") ?: 0L,
                lastAutoApplyCount = doc.getLong("lastAutoApplyCount") ?: 0L
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fullName" to fullName,
            "headline" to headline,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "bio" to bio,
            "skills" to skills,
            "experienceYears" to experienceYears,
            "preferredRole" to preferredRole,
            "currentCompany" to currentCompany,
            "highestDegree" to highestDegree,
            "workPreference" to workPreference,
            "jobType" to jobType,
            "preferredLocations" to preferredLocations,
            "salaryRange" to salaryRange,
            "resumeUrl" to resumeUrl,
            "resumeText" to resumeText,
            "profileImageUrl" to profileImageUrl,
            "linkedinUrl" to linkedinUrl,
            "githubUrl" to githubUrl,
            "portfolioUrl" to portfolioUrl,
            "twitterUrl" to twitterUrl,
            "currentCtc" to currentCtc,
            "expectedCtc" to expectedCtc,
            "noticePeriod" to noticePeriod,
            "profileCompleted" to profileCompleted,
            "isPremium" to isPremium,
            "premiumPlan" to premiumPlan,
            "premiumExpiry" to premiumExpiry,
            "emailLimit" to emailLimit,
            "emailsSent" to emailsSent,
            "emailSubjectTemplate" to emailSubjectTemplate,
            "emailBodyTemplate" to emailBodyTemplate,
            "emailAutoSend" to emailAutoSend,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "autoApplyEnabled" to autoApplyEnabled
        )
    }

    fun isPremiumActive(): Boolean {
        if (premiumPlan == "free") return false
        if (premiumExpiry == 0L) return true // Lifetime or manual active
        return premiumExpiry > System.currentTimeMillis()
    }

    fun getRemainingEmails(): Long {
        val limit = if (emailLimit > 0) emailLimit else when (premiumPlan) {
            "starter" -> 100L
            "pro" -> 500L
            "unlimited" -> 5000L
            else -> 10L
        }
        return (limit - emailsSent).coerceAtLeast(0)
    }

    fun canSendEmail(): Boolean {
        return isPremiumActive() || getRemainingEmails() > 0
    }
}