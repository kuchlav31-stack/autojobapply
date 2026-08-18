package com.dark.jobai.data.model

/**
 * Email template data model
 */
data class EmailTemplate(
    val subjectTemplate: String = "Application for {job_title} at {company}",
    val bodyTemplate: String = """Dear Hiring Manager,

I hope this email finds you well.

My name is {applicant_name}, and I am writing to express my strong interest in the {job_title} position at {company}.

After researching your company and understanding the requirements of this role, I am confident that my skills and experience make me an excellent candidate for this position.

{resume_section}

I would welcome the opportunity to discuss how my background and skills would benefit your team.

Thank you for considering my application. I look forward to hearing from you.

Best regards,
{applicant_name}""",
    val autoSend: Boolean = true
) {
    /**
     * Replace placeholders with actual values
     */
    fun replacePlaceholders(
        jobTitle: String = "",
        companyName: String = "",
        applicantName: String = "",
        applicantEmail: String = "",
        resumeUrl: String = "",
        linkedinUrl: String = "",
        portfolioUrl: String = ""
    ): Pair<String, String> {
        val subject = subjectTemplate
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)
            .replace("{applicant_name}", applicantName)

        val body = bodyTemplate
            .replace("{job_title}", jobTitle)
            .replace("{company}", companyName)
            .replace("{applicant_name}", applicantName)
            .replace("{applicant_email}", applicantEmail)
            .replace("{resume_url}", resumeUrl)
            .replace("{linkedin_url}", linkedinUrl)
            .replace("{portfolio_url}", portfolioUrl)
            .replace(
                "{resume_section}",
                if (resumeUrl.isNotEmpty()) "My Resume: $resumeUrl" else ""
            )

        return Pair(subject, body)
    }

    companion object {
        /**
         * Get default template
         */
        fun getDefault(): EmailTemplate {
            return EmailTemplate()
        }

        /**
         * Get placeholders list for UI display
         */
        fun getPlaceholders(): List<Pair<String, String>> {
            return listOf(
                "{job_title}" to "Job title",
                "{company}" to "Company name",
                "{applicant_name}" to "Your name",
                "{applicant_email}" to "Your email",
                "{resume_url}" to "Resume link",
                "{linkedin_url}" to "LinkedIn URL",
                "{portfolio_url}" to "Portfolio URL"
            )
        }
    }
}