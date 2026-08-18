package com.dark.jobai.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val companyLogo: String = "",
    val location: String = "",
    val workType: String = "Remote",
    val salary: String = "Not Disclosed",
    val experienceRequired: String = "Not Disclosed",
    val description: String = "",
    val applyUrl: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val source: String = "",
    val postedAt: Long = 0L,
    val tags: List<String> = emptyList(),
    val hasEmail: Boolean = false,
    val hasPhone: Boolean = false,
    val isActive: Boolean = true,
    val isSaved: Boolean = false,
    val matchScore: Int = 0
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Job {
            return Job(
                id = doc.id,
                title = doc.getString("title") ?: "",
                company = doc.getString("company") ?: "Unknown Company",
                companyLogo = doc.getString("companyLogo") ?: "",
                location = doc.getString("location") ?: "Remote",
                workType = doc.getString("workType") ?: "Remote",
                salary = doc.getString("salary") ?: "Not Disclosed",
                experienceRequired = doc.getString("experienceRequired") ?: "Not Disclosed",
                description = doc.getString("description") ?: "",
                applyUrl = doc.getString("applyUrl") ?: "",
                contactEmail = doc.getString("contactEmail") ?: "",
                contactPhone = doc.getString("contactPhone") ?: "",
                source = doc.getString("source") ?: "",
                postedAt = doc.getLong("postedAt") ?: 0L,
                tags = (doc.get("tags") as? List<String>) ?: emptyList(),
                hasEmail = doc.getBoolean("hasEmail") ?: false,
                hasPhone = doc.getBoolean("hasPhone") ?: false,
                isActive = doc.getBoolean("isActive") ?: true,
                isSaved = doc.getBoolean("isSaved") ?: false,
                matchScore = (doc.getLong("matchScore") ?: 0L).toInt()
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "company" to company,
            "companyLogo" to companyLogo,
            "location" to location,
            "workType" to workType,
            "salary" to salary,
            "experienceRequired" to experienceRequired,
            "description" to description,
            "applyUrl" to applyUrl,
            "contactEmail" to contactEmail,
            "contactPhone" to contactPhone,
            "source" to source,
            "postedAt" to postedAt,
            "tags" to tags,
            "hasEmail" to hasEmail,
            "hasPhone" to hasPhone,
            "isActive" to isActive,
            "isSaved" to isSaved,
            "matchScore" to matchScore
        )
    }
}