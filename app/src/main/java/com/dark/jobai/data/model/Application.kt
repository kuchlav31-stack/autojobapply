package com.dark.jobai.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Application(
    val id: String = "",
    val userId: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val toEmail: String = "",
    val applicantName: String = "",
    val status: String = "Applied",
    val emailStatus: String = "Sent",
    val appliedAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val openedAt: Long = 0L,
    val clickedAt: Long = 0L,
    val bouncedAt: Long = 0L,
    val messageId: String = "",
    val emailSubject: String = "",
    val emailBody: String = "",
    val applicationType: String = "Email"
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Application {
            return Application(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                jobId = doc.getString("jobId") ?: "",
                jobTitle = doc.getString("jobTitle") ?: "",
                companyName = doc.getString("companyName") ?: "",
                toEmail = doc.getString("toEmail") ?: "",
                applicantName = doc.getString("applicantName") ?: "",
                status = doc.getString("status") ?: "Applied",
                emailStatus = doc.getString("emailStatus") ?: "Sent",
                appliedAt = doc.getLong("appliedAt") ?: 0L,
                deliveredAt = doc.getLong("deliveredAt") ?: 0L,
                openedAt = doc.getLong("openedAt") ?: 0L,
                clickedAt = doc.getLong("clickedAt") ?: 0L,
                bouncedAt = doc.getLong("bouncedAt") ?: 0L,
                messageId = doc.getString("messageId") ?: "",
                emailSubject = doc.getString("emailSubject") ?: "",
                emailBody = doc.getString("emailBody") ?: "",
                applicationType = doc.getString("applicationType") ?: "Email"
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "jobId" to jobId,
            "jobTitle" to jobTitle,
            "companyName" to companyName,
            "toEmail" to toEmail,
            "applicantName" to applicantName,
            "status" to status,
            "emailStatus" to emailStatus,
            "appliedAt" to appliedAt,
            "deliveredAt" to deliveredAt,
            "openedAt" to openedAt,
            "clickedAt" to clickedAt,
            "bouncedAt" to bouncedAt,
            "messageId" to messageId,
            "emailSubject" to emailSubject,
            "emailBody" to emailBody,
            "applicationType" to applicationType
        )
    }
}