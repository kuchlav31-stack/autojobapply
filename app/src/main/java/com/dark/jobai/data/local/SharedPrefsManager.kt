package com.dark.jobai.data.local

import android.content.Context
import android.content.SharedPreferences
import com.dark.jobai.util.Constants

/**
 * SharedPrefsManager - Local storage for app preferences
 */
class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // ============ Dark Mode ============

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(Constants.KEY_DARK_MODE, true)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply()
    }

    // ============ Email Template ============

    fun isEmailTemplateSaved(): Boolean {
        return prefs.getBoolean(Constants.KEY_EMAIL_TEMPLATE_SAVED, false)
    }

    fun setEmailTemplateSaved(saved: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_EMAIL_TEMPLATE_SAVED, saved).apply()
    }

    fun isAutoSendEnabled(): Boolean {
        return prefs.getBoolean(Constants.KEY_AUTO_SEND, false)
    }

    fun setAutoSend(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_AUTO_SEND, enabled).apply()
    }

    // ============ Generic ============

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs.getLong(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}