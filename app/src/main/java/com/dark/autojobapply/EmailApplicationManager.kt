package com.dark.autojobapply

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EmailApplicationManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "email_application_prefs",
        Context.MODE_PRIVATE
    )

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val KEY_TEMPLATE_SAVED = "template_saved"
        const val KEY_AUTO_SEND = "auto_send_enabled"

        // Cloud Function URLs
        const val SEND_EMAIL_URL = "https://us-central1-autojobapply-a57ca.cloudfunctions.net/sendJobApplicationEmail"
        const val SAVE_TEMPLATE_URL = "https://us-central1-autojobapply-a57ca.cloudfunctions.net/saveEmailTemplate"
    }

    fun isTemplateSaved(): Boolean {
        return sharedPrefs.getBoolean(KEY_TEMPLATE_SAVED, false)
    }

    fun isAutoSendEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_AUTO_SEND, false)
    }

    fun saveEmailTemplateLocally(subject: String, body: String, autoSend: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_TEMPLATE_SAVED, true)
            .putString("subject_template", subject)
            .putString("body_template", body)
            .putBoolean(KEY_AUTO_SEND, autoSend)
            .apply()
        Log.d("EmailManager", "✅ Template saved locally")
    }

    /**
     * Save template - Local + Server (Firestore via Cloud Function)
     */
    suspend fun saveTemplateToServer(
        subjectTemplate: String,
        bodyTemplate: String,
        autoSend: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid

                if (userId == null) {
                    withContext(Dispatchers.Main) { onError("User not authenticated") }
                    return@withContext
                }

                // पहले LOCAL save करो - हमेशा success
                saveEmailTemplateLocally(subjectTemplate, bodyTemplate, autoSend)

                // फिर Firestore में DIRECT save करो (Cloud Function के बिना)
                try {
                    db.collection("users").document(userId).set(
                        mapOf(
                            "emailSubjectTemplate" to subjectTemplate,
                            "emailBodyTemplate" to bodyTemplate,
                            "emailAutoSend" to autoSend
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()

                    Log.d("EmailManager", "✅ Template saved to Firestore directly")
                } catch (e: Exception) {
                    Log.w("EmailManager", "⚠️ Firestore save failed, but local saved")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e("EmailManager", "Save error", e)
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to save")
                }
            }
        }
    }

    /**
     * Send email via Cloud Function
     */
    suspend fun sendEmailDirectly(
        toEmail: String,
        jobTitle: String,
        companyName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid

                if (userId == null) {
                    withContext(Dispatchers.Main) { onError("User not authenticated") }
                    return@withContext
                }

                // Check user document exists
                val userDoc = try {
                    db.collection("users").document(userId).get().await()
                } catch (e: Exception) {
                    null
                }

                if (userDoc == null || !userDoc.exists()) {
                    Log.e("EmailManager", "User document not found in Firestore")
                    withContext(Dispatchers.Main) {
                        onError("❌ Profile not found. Please complete your profile first.")
                    }
                    return@withContext
                }

                Log.d("EmailManager", "📧 Sending email...")
                Log.d("EmailManager", "To: $toEmail")
                Log.d("EmailManager", "Job: $jobTitle at $companyName")
                Log.d("EmailManager", "User: $userId")

                val jsonData = JSONObject().apply {
                    put("toEmail", toEmail)
                    put("jobTitle", jobTitle)
                    put("companyName", companyName)
                    put("userId", userId)
                }

                val result = makeHttpRequest(SEND_EMAIL_URL, jsonData.toString())

                if (result.first) {
                    Log.d("EmailManager", "✅ Email sent successfully")
                    withContext(Dispatchers.Main) {
                        onSuccess("✅ Email sent successfully!")
                    }
                } else {
                    Log.e("EmailManager", "❌ Server error: ${result.second}")
                    withContext(Dispatchers.Main) {
                        onError("❌ Failed to send email. Please try again.")
                    }
                }

            } catch (e: Exception) {
                Log.e("EmailManager", "Exception", e)
                withContext(Dispatchers.Main) {
                    onError("❌ ${e.localizedMessage ?: "Failed"}")
                }
            }
        }
    }

    /**
     * HTTP POST Request
     */
    private fun makeHttpRequest(urlString: String, jsonBody: String): Pair<Boolean, String> {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            Log.d("EmailManager", "URL: $urlString")
            Log.d("EmailManager", "Body: $jsonBody")

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d("EmailManager", "Response Code: $responseCode")

            when (responseCode) {
                in 200..299 -> {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d("EmailManager", "Response: $response")
                    Pair(true, response)
                }
                else -> {
                    val error = try {
                        connection.errorStream?.bufferedReader()?.readText()
                    } catch (e: Exception) {
                        null
                    } ?: "HTTP $responseCode"

                    Log.e("EmailManager", "Error: $error")
                    Pair(false, error)
                }
            }

        } catch (e: Exception) {
            Log.e("EmailManager", "Network error: ${e.localizedMessage}", e)
            Pair(false, e.localizedMessage ?: "Network error")
        } finally {
            connection?.disconnect()
        }
    }
}