package com.dark.jobai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dark.jobai.navigation.AppNavigation
import com.dark.jobai.service.UpiPaymentService
import com.dark.jobai.ui.theme.JobAITheme

class MainActivity : ComponentActivity() {

    private lateinit var upiPaymentService: UpiPaymentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UPI Payment Service
        upiPaymentService = UpiPaymentService(this)

        enableEdgeToEdge()

        setContent {
            JobAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        upiPaymentService.handlePaymentResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data,
            onSuccess = { planName ->
                Toast.makeText(
                    this,
                    "✅ $planName activated successfully!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }
}