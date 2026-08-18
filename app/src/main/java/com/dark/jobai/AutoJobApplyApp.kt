package com.dark.jobai

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
//        settings.isPersistenceEnabled = true
        firestore.firestoreSettings = settings
    }
}