package com.dark.jobai.data.repository

import android.util.Log
import com.dark.jobai.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * EmailRepository - Handles email sending via Cloud Functions
 */
class EmailRepository {

    companion object {
        private const val TAG = "EmailRepository"
    }

    /**
     * Send job application email
     */
    suspend fun sendApplicationEmail(
        userId: String,
        toEmail: String,
        jobTitle: String,
        companyName: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("toEmail", toEmail)
                    put("jobTitle", jobTitle)
                    put("companyName", companyName)
                }

                val result = makeHttpRequest(Constants.URL_SEND_EMAIL, jsonData.toString())

                if (result.first) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(result.second))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send email", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Save email template
     */
    suspend fun saveEmailTemplate(
        userId: String,
        subjectTemplate: String,
        bodyTemplate: String,
        autoSend: Boolean
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("subjectTemplate", subjectTemplate)
                    put("bodyTemplate", bodyTemplate)
                    put("autoSend", autoSend)
                }

                val result = makeHttpRequest(Constants.URL_SAVE_TEMPLATE, jsonData.toString())

                if (result.first) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(result.second))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Test email
     */
    suspend fun sendTestEmail(toEmail: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonData = JSONObject().apply {
                    put("toEmail", toEmail)
                }

                val result = makeHttpRequest(Constants.URL_TEST_EMAIL, jsonData.toString())

                if (result.first) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(result.second))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Make HTTP POST request
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

            Log.d(TAG, "URL: $urlString")
            Log.d(TAG, "Body: $jsonBody")

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response Code: $responseCode")

            when (responseCode) {
                in 200..299 -> {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d(TAG, "Response: $response")
                    Pair(true, response)
                }
                else -> {
                    val error = try {
                        connection.errorStream?.bufferedReader()?.readText()
                    } catch (e: Exception) {
                        null
                    } ?: "HTTP $responseCode"

                    Log.e(TAG, "Error: $error")
                    Pair(false, error)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Network error", e)
            Pair(false, e.localizedMessage ?: "Network error")
        } finally {
            connection?.disconnect()
        }
    }
}