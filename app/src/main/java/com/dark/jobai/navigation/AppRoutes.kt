package com.dark.jobai.navigation

object AppRoutes {
    // Auth
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"

    // Onboarding
    const val PROFILE_SETUP = "profile_setup"
    const val RESUME_UPLOAD = "resume_upload"
    const val EMAIL_TEMPLATE = "email_template"

    // Main
    const val MAIN = "main"

    // Detail screens
    const val JOB_DETAIL = "job_detail/{jobId}"
    const val CREATE_POST = "create_post"
    const val APPLICATION_DETAIL = "application_detail/{applicationId}"
    const val PRICING = "pricing"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val USER_PROFILE = "user_profile"
    const val WEB_VIEW = "web_view/{url}"

    // Helper functions


    fun jobDetail(jobId: String) = "job_detail/$jobId"
    fun applicationDetail(appId: String) = "application_detail/$appId"
    fun webView(url: String) = "web_view/${android.net.Uri.encode(url)}"
}