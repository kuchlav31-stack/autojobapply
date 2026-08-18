package com.dark.autojobapply
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.auth.api.signin.GoogleSignInClient
object AppRoutes {
    const val SPLASH = "splash_screen"
    const val LOGIN = "login_screen"
    const val SIGNUP = "signup_screen"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home_screen"
    const val EMAIL_TEMPLATE = "email_template"
    const val APPLICATIONS = "applications"
    const val USER_PROFILE = "user_profile"
    const val MAIN = "main_screen"

}
@Composable
fun AppNavigation(
    navController: NavHostController,
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>,
    googleSignInLoading: Boolean,
    onGoogleSignInLoadingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Helper function to start Google Sign-In intent
    fun launchGoogleSignIn() {
        onGoogleSignInLoadingChange(true)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen { route ->
                navController.navigate(route) {
                    popUpTo(AppRoutes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoutes.SIGNUP)
                },
                onGoogleSignInClick = {
                    launchGoogleSignIn()
                },
                onShowMessage = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                },
                googleSignInLoading = googleSignInLoading
            )
        }

        composable(AppRoutes.SIGNUP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onGoogleSignUpClick = {
                    launchGoogleSignIn()
                },
                onShowMessage = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                },
                googleSignInLoading = googleSignInLoading
            )
        }

        // PROFILE SETUP SCREEN
        composable(AppRoutes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.PROFILE_SETUP) { inclusive = true }
                    }
                },
                onShowMessage = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            )
        }

        // HOME SCREEN - with onProfileNeeded callback

// Add composable
        composable(AppRoutes.EMAIL_TEMPLATE) {
            EmailTemplateScreen(
                onTemplateSaved = {
                    navController.popBackStack()
                },
                onShowMessage = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            )
        }
        composable(AppRoutes.APPLICATIONS) {
            ApplicationDashboard(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.USER_PROFILE) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
// Update HOME composable
        composable(AppRoutes.HOME) {
            MainHomeScreen(
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                },
                onProfileNeeded = {
                    navController.navigate(AppRoutes.PROFILE_SETUP)
                },
                onEmailTemplateNeeded = {
                    navController.navigate(AppRoutes.EMAIL_TEMPLATE)
                }
            )
        }    }
}