package com.dark.autojobapply

import com.google.firebase.firestore.DocumentSnapshot

data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val companyLogo: String = "",
    val location: String = "",
    val workType: String = "",
    val salary: String = "",
    val experienceRequired: String = "",
    val description: String = "",
    val applyUrl: String = "",
    val contactEmail: String = "",      // NEW
    val contactPhone: String = "",      // NEW
    val source: String = "",
    val postedAt: Long = 0L,
    val tags: List<String> = emptyList(),
    val hasEmail: Boolean = false,       // NEW
    val hasPhone: Boolean = false,       // NEW
    val isActive: Boolean = true,
    val isSaved: Boolean = false
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Job {
            return Job(
                id = doc.id,
                title = doc.getString("title") ?: "",
                company = doc.getString("company") ?: "",
                companyLogo = doc.getString("companyLogo") ?: "",
                location = doc.getString("location") ?: "",
                workType = doc.getString("workType") ?: "",
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
                isSaved = doc.getBoolean("isSaved") ?: false
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
            "isSaved" to isSaved
        )
    }
}