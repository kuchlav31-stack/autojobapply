package com.dark.autojobapply

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dark.autojobapply.ui.theme.AutojobapplyTheme
import com.dark.autojobapply.ui.theme.BackgroundDark
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Shared loading state for Google Sign-In (passed to screens)
    private var googleSignInLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Register launcher for Google Sign-In result
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                googleSignInLoading = false
                Toast.makeText(
                    this,
                    "Google Sign-In failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            AutojobapplyTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                ) { innerPadding ->
                    val navController = rememberNavController()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation(
                            navController = navController,
                            googleSignInClient = googleSignInClient,
                            googleSignInLauncher = googleSignInLauncher,
                            googleSignInLoading = googleSignInLoading,
                            onGoogleSignInLoadingChange = { loading ->
                                googleSignInLoading = loading
                            }
                        )
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        ensureFirestoreUser(
                            uid = user.uid,
                            name = user.displayName ?: "Google User",
                            email = user.email ?: "",
                            provider = "Google OAuth"
                        )
                    }
                    googleSignInLoading = false
                } else {
                    googleSignInLoading = false
                    Toast.makeText(
                        this,
                        "Google Authentication failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun ensureFirestoreUser(uid: String, name: String, email: String, provider: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val payload = hashMapOf(
                        "uid" to uid,
                        "fullName" to name,
                        "email" to email,
                        "provider" to provider,
                        "createdAt" to System.currentTimeMillis(),
                        "skills" to emptyList<String>(),
                        "preferredRoles" to emptyList<String>(),
                        "experienceYears" to "",
                        "workPreference" to "Remote"
                    )
                    userRef.set(payload)
                }
                googleSignInLoading = false
            }
            .addOnFailureListener { e ->
                googleSignInLoading = false
                Toast.makeText(
                    this,
                    "Database sync failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}