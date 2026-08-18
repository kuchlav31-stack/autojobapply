package com.dark.jobai.service

import android.util.Log
import com.dark.jobai.data.model.EmailTemplate
import com.dark.jobai.data.repository.EmailRepository
import com.dark.jobai.data.repository.UserRepository

/**
 * EmailService - High-level email operations
 */
class EmailService(
    private val emailRepository: EmailRepository = EmailRepository(),
    private val userRepository: UserRepository = UserRepository()
) {

    companion object {
        private const val TAG = "EmailService"
    }

    /**
     * Send application email with user's custom template
     */
    suspend fun sendApplicationEmail(
        userId: String,
        toEmail: String,
        jobTitle: String,
        companyName: String
    ): Result<String> {
        return try {
            // Get user data
            val user = userRepository.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))

            // Check premium status
            if (!user.canSendEmail()) {
                return Result.failure(Exception("Email limit reached. Please upgrade to premium."))
            }

            // Send email via Cloud Function
            val result = emailRepository.sendApplicationEmail(
                userId = userId,
                toEmail = toEmail,
                jobTitle = jobTitle,
                companyName = companyName
            )

            if (result.isSuccess) {
                // Increment email count
                userRepository.incrementEmailCount(userId)
                Result.success("Email sent successfully!")
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to send email"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send email failed", e)
            Result.failure(e)
        }
    }

    /**
     * Save email template
     */
    suspend fun saveEmailTemplate(
        userId: String,
        template: EmailTemplate
    ): Result<Unit> {
        return try {
            // Save to Firestore
            val firestoreResult = userRepository.saveEmailTemplate(
                userId = userId,
                subjectTemplate = template.subjectTemplate,
                bodyTemplate = template.bodyTemplate,
                autoSend = template.autoSend
            )

            if (firestoreResult) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save template"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get email template for user
     */
    suspend fun getEmailTemplate(userId: String): EmailTemplate? {
        return try {
            val user = userRepository.getUserById(userId) ?: return null

            EmailTemplate(
                subjectTemplate = user.emailSubjectTemplate.ifEmpty {
                    EmailTemplate.getDefault().subjectTemplate
                },
                bodyTemplate = user.emailBodyTemplate.ifEmpty {
                    EmailTemplate.getDefault().bodyTemplate
                },
                autoSend = user.emailAutoSend
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Send test email
     */
    suspend fun sendTestEmail(toEmail: String): Result<Unit> {
        return emailRepository.sendTestEmail(toEmail)
    }
}