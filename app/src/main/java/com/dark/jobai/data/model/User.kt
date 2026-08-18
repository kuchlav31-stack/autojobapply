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
    val workPreference: String = "Remote",
    val jobType: String = "All",              // NEW: All, Remote, On-site, Hybrid
    val preferredLocations: List<String> = emptyList(),  // NEW
    val salaryRange: String = "",              // NEW
    val resumeUrl: String = "",
    val resumeText: String = "",
    val profileImageUrl: String = "",
    val linkedinUrl: String = "",
    val githubUrl: String = "",
    val portfolioUrl: String = "",
    val twitterUrl: String = "",
    val currentSalary: String = "",
    val expectedSalary: String = "",
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
                currentSalary = doc.getString("currentSalary") ?: "",
                expectedSalary = doc.getString("expectedSalary") ?: "",
                noticePeriod = doc.getString("noticePeriod") ?: "",
                profileCompleted = doc.getBoolean("profileCompleted") ?: false,
                isPremium = doc.getBoolean("isPremium") ?: false,
                premiumPlan = doc.getString("premiumPlan") ?: "free",
                premiumExpiry = doc.getLong("premiumExpiry") ?: 0L,
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
            "currentSalary" to currentSalary,
            "expectedSalary" to expectedSalary,
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
            "updatedAt" to updatedAt
        )
    }

    fun isPremiumActive(): Boolean {
        if (!isPremium) return false
        if (premiumExpiry == 0L) return true
        return premiumExpiry > System.currentTimeMillis()
    }

    fun getRemainingEmails(): Long {
        return (emailLimit - emailsSent).coerceAtLeast(0)
    }

    fun canSendEmail(): Boolean {
        return isPremiumActive() && getRemainingEmails() > 0
    }
}