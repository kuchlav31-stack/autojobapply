package com.dark.jobai
import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val fbLogger = AppEventsLogger.newLogger(context)

    fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun trackAppOpen() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        fbLogger.logEvent("auto_job_app_opened")
    }

    fun trackJobApplied(jobTitle: String) {
        val params = Bundle().apply {
            putString("job_title", jobTitle)
        }
        firebaseAnalytics.logEvent("job_applied", params)
        fbLogger.logEvent("fb_mobile_complete_registration", params)
    }
}