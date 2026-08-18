package com.dark.jobai.data.model

/**
 * Pricing plan data model
 */
data class Plan(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val period: String = "/month",
    val description: String = "",
    val emailLimit: Long = 0,
    val features: List<String> = emptyList(),
    val isRecommended: Boolean = false,
    val gradientColors: List<Long> = emptyList(),  // Color values
    val badgeText: String = ""
) {
    companion object {
        /**
         * Get all available plans
         */
        fun getAllPlans(): List<Plan> {
            return listOf(
                Plan(
                    id = "premium_monthly",
                    name = "Premium",
                    price = 299,
                    period = "/month",
                    description = "For serious job seekers",
                    emailLimit = 50,
                    features = listOf(
                        "50 Email Auto-Apply/month",
                        "Email Tracking (Opened/Delivered)",
                        "Application Dashboard",
                        "Custom Email Template",
                        "AI Job Matching Score",
                        "Priority Support"
                    ),
                    isRecommended = true,
                    badgeText = "MOST POPULAR"
                ),
                Plan(
                    id = "pro_monthly",
                    name = "Pro",
                    price = 599,
                    period = "/month",
                    description = "For active job seekers",
                    emailLimit = 200,
                    features = listOf(
                        "200 Email Auto-Apply/month",
                        "Auto-Apply Mode (Daily 10)",
                        "AI Generated Cover Letter",
                        "Recruiter Outreach",
                        "3 Multiple Resumes",
                        "Advanced Analytics",
                        "Priority Support"
                    ),
                    isRecommended = false
                ),
                Plan(
                    id = "agency_monthly",
                    name = "Agency",
                    price = 1499,
                    period = "/month",
                    description = "For agencies & recruiters",
                    emailLimit = 1000,
                    features = listOf(
                        "1000 Email Auto-Apply/month",
                        "Auto-Apply Mode (Daily 50)",
                        "Bulk Apply (50 jobs at once)",
                        "10 Multiple Resumes",
                        "3 Team Members",
                        "API Access",
                        "Dedicated Support"
                    ),
                    isRecommended = false
                )
            )
        }

        /**
         * Get plan by ID
         */
        fun getPlanById(planId: String): Plan? {
            return getAllPlans().find { it.id == planId }
        }
    }
}