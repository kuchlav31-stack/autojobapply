package com.dark.jobai.util

object Constants {
    // Firebase Collections
    const val COLLECTION_JOBS = "jobs"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_FEED = "feed"
    const val COLLECTION_PAYMENTS = "payments"
    const val COLLECTION_APPLICATIONS = "applications"
    const val COLLECTION_SAVED_JOBS = "savedJobs"


    // Cloud Function URLs
    const val CLOUD_FUNCTION_BASE = "https://us-central1-autojobapply-a57ca.cloudfunctions.net"
    const val URL_SEND_EMAIL = "$CLOUD_FUNCTION_BASE/sendJobApplicationEmail"
    const val URL_SAVE_TEMPLATE = "$CLOUD_FUNCTION_BASE/saveEmailTemplate"
    const val URL_TEST_EMAIL = "$CLOUD_FUNCTION_BASE/testEmail"
    const val URL_MANUAL_JOB_FETCH = "$CLOUD_FUNCTION_BASE/manualJobFetch"

    // Brevo
    const val BREVO_SENDER_EMAIL = "kuchlav31@gmail.com"
    const val BREVO_SENDER_NAME = "JobAI"

    // Razorpay
    const val RAZORPAY_KEY_ID = "rzp_live_6vd9RApruseTAi"

    // Premium Plans
    const val PLAN_FREE = "free"
    const val PLAN_PREMIUM = "premium_monthly"
    const val PLAN_PRO = "pro_monthly"
    const val PLAN_AGENCY = "agency_monthly"

    const val PRICE_PREMIUM = 2
    const val PRICE_PRO = 599
    const val PRICE_AGENCY = 1499

    // Email Limits
    const val FREE_EMAIL_LIMIT = 0
    const val PREMIUM_EMAIL_LIMIT = 50
    const val PRO_EMAIL_LIMIT = 200
    const val AGENCY_EMAIL_LIMIT = 1000

    // Shared Preferences
    const val PREFS_NAME = "jobai_prefs"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_EMAIL_TEMPLATE_SAVED = "email_template_saved"
    const val KEY_AUTO_SEND = "auto_send_enabled"
}