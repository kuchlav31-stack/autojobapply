package com.dark.autojobapply

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Manages email application system
 * Handles:
 * 1. Email template storage (one-time setup)
 * 2. Auto-send without editing
 * 3. Edit before sending
 */
class EmailApplicationManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "email_application_prefs",
        Context.MODE_PRIVATE
    )

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val KEY_TEMPLATE_SAVED = "template_saved"
        const val KEY_EMAIL_TEMPLATE = "email_template"
        const val KEY_AUTO_SEND = "auto_send_enabled"
        const val KEY_SUBJECT_TEMPLATE = "subject_template"
    }

    /**
     * Check if email template is saved
     */
    fun isTemplateSaved(): Boolean {
        return sharedPrefs.getBoolean(KEY_TEMPLATE_SAVED, false)
    }

    /**
     * Check if auto-send is enabled
     */
    fun isAutoSendEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_AUTO_SEND, false)
    }

    /**
     * Save email template (one-time setup)
     */
    fun saveEmailTemplate(
        subject: String,
        body: String,
        autoSend: Boolean
    ) {
        sharedPrefs.edit()
            .putBoolean(KEY_TEMPLATE_SAVED, true)
            .putString(KEY_SUBJECT_TEMPLATE, subject)
            .putString(KEY_EMAIL_TEMPLATE, body)
            .putBoolean(KEY_AUTO_SEND, autoSend)
            .apply()
    }

    /**
     * Get saved email template
     */
    fun getEmailTemplate(): Pair<String, String> {
        val subject = sharedPrefs.getString(KEY_SUBJECT_TEMPLATE, "") ?: ""
        val body = sharedPrefs.getString(KEY_EMAIL_TEMPLATE, "") ?: ""
        return Pair(subject, body)
    }

    /**
     * Enable/disable auto-send
     */
    fun setAutoSend(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_AUTO_SEND, enabled)
            .apply()
    }

    /**
     * Get default email template with user data filled
     */
    suspend fun getDefaultTemplateWithUserData(): Pair<String, String> {
        val userId = auth.currentUser?.uid ?: return Pair("", "")

        return try {
            val doc = db.collection("users").document(userId).get().await()

            if (doc.exists()) {
                val name = doc.getString("fullName") ?: ""
                val headline = doc.getString("headline") ?: ""
                val skills = doc.get("skills") as? List<String> ?: emptyList()
                val experience = doc.getString("experienceYears") ?: ""
                val location = doc.getString("location") ?: ""
                val linkedin = doc.getString("linkedinUrl") ?: ""
                val portfolio = doc.getString("portfolioUrl") ?: ""
                val resumeUrl = doc.getString("resumeUrl") ?: ""

                val subject = "Application for the position"

                val body = buildString {
                    append("Dear Hiring Manager,\n\n")
                    append("I hope this email finds you well.\n\n")

                    if (name.isNotEmpty()) {
                        append("My name is $name")
                    }

                    if (headline.isNotEmpty()) {
                        append(", and I am a $headline")
                    }
                    append(".\n\n")

                    append("I am writing to express my interest in this position. ")

                    if (experience.isNotEmpty()) {
                        append("I have $experience years of professional experience. ")
                    }

                    if (skills.isNotEmpty()) {
                        append("\n\nMy key skills include: ${skills.take(8).joinToString(", ")}.")
                    }

                    if (location.isNotEmpty()) {
                        append("\n\nLocation: $location")
                    }

                    if (resumeUrl.isNotEmpty()) {
                        append("\nResume: $resumeUrl")
                    }

                    if (linkedin.isNotEmpty()) {
                        append("\nLinkedIn: $linkedin")
                    }

                    if (portfolio.isNotEmpty()) {
                        append("\nPortfolio: $portfolio")
                    }

                    append("\n\nI am excited about the opportunity to contribute to your team and would welcome the chance to discuss my qualifications further.\n\n")
                    append("Best regards,\n")
                    append(name.ifEmpty { "Applicant" })
                }

                Pair(subject, body)
            } else {
                getDefaultTemplate()
            }
        } catch (e: Exception) {
            getDefaultTemplate()
        }
    }

    /**
     * Simple default template
     */
    private fun getDefaultTemplate(): Pair<String, String> {
        val subject = "Job Application"
        val body = "Dear Hiring Manager,\n\nI am interested in this position. Please find my resume attached.\n\nBest regards"
        return Pair(subject, body)
    }

    /**
     * Send email directly (one-click apply)
     */
    fun sendEmailDirectly(
        toEmail: String,
        jobTitle: String,
        companyName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Get template
        val (savedSubject, savedBody) = getEmailTemplate()

        // Replace placeholders
        val subject = savedSubject
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)

        val body = savedBody
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)

        sendEmail(
            toEmail = toEmail,
            subject = subject,
            body = body,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Send email with custom content
     */
    fun sendEmail(
        toEmail: String,
        subject: String,
        body: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            context.startActivity(Intent.createChooser(intent, "Send Email"))
            onSuccess()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Failed to open email client")
        }
    }

    /**
     * Open email with editable content (user can modify before sending)
     */
    fun openEmailWithEdit(
        toEmail: String,
        jobTitle: String,
        companyName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Get template and replace placeholders
        val (savedSubject, savedBody) = getEmailTemplate()

        val subject = savedSubject
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)

        val body = savedBody
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            context.startActivity(Intent.createChooser(intent, "Edit & Send Email"))
            onSuccess()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Failed to open email client")
        }
    }

    /**
     * Check if user has email app installed
     */
    fun hasEmailApp(): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
        }
        return intent.resolveActivity(context.packageManager) != null
    }
}