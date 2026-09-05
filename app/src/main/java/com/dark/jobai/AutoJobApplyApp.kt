package com.dark.jobai

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

class AutoJobApplyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Pre-initialize services for faster access
        FirebaseAuth.getInstance()
        FirebaseFirestore.getInstance()
        FirebaseStorage.getInstance()

        // Apply Firestore settings
        val firestore = FirebaseFirestore.getInstance()
        val settings = firestore.firestoreSettings
        firestore.firestoreSettings = settings

        // Fetch & Sync FCM Push Notification Token on App Startup
        fetchAndSyncFCMToken()
    }

    private fun fetchAndSyncFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !token.isNullOrEmpty()) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM token successfully synced to Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to sync FCM token", e)
                    }
            }
        }
    }
}