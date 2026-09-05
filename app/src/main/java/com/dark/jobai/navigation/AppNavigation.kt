package com.dark.jobai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dark.autojobapply.SplashScreen
import com.dark.jobai.ui.screens.auth.ForgotPasswordScreen
import com.dark.jobai.ui.screens.auth.LoginScreen
import com.dark.jobai.ui.screens.auth.OnboardingScreen
import com.dark.jobai.ui.screens.auth.SignUpScreen
import com.dark.jobai.ui.screens.main.MainScreen
import com.dark.jobai.ui.screens.main.jobs.JobDetailScreen
import com.dark.jobai.ui.screens.main.pricing.PricingScreen
import com.dark.jobai.ui.screens.main.profile.EditProfileScreen
import com.dark.jobai.ui.screens.main.profile.SettingsScreen
import com.dark.jobai.ui.screens.onboarding.EmailTemplateScreen
import com.dark.jobai.ui.screens.onboarding.ProfileSetupScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        // ============ SPLASH ============
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ============ ONBOARDING CAROUSEL ============
        composable(AppRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ============ AUTH ============
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoutes.SIGNUP)
                }
            )
        }

        composable(AppRoutes.SIGNUP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AppRoutes.PROFILE_SETUP) {
                        popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ============ ONBOARDING SETUP ============
        composable(AppRoutes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.PROFILE_SETUP) { inclusive = true }
                    }
                },
                onShowMessage = { }
            )
        }

        composable(AppRoutes.EMAIL_TEMPLATE) {
            EmailTemplateScreen(
                onTemplateSaved = { navController.popBackStack() },
                onShowMessage = { }
            )
        }

        // ============ MAIN ============
        composable(AppRoutes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.MAIN) { inclusive = true }
                    }
                },
                onProfileNeeded = {
                    navController.navigate(AppRoutes.PROFILE_SETUP)
                },
                onEmailTemplateNeeded = {
                    navController.navigate(AppRoutes.EMAIL_TEMPLATE)
                },
                onUpgradeClick = {
                    navController.navigate(AppRoutes.PRICING)
                },
                onJobDetailClick = { jobId ->
                    navController.navigate(AppRoutes.jobDetail(jobId))
                },
                onEditProfileClick = {
                    navController.navigate(AppRoutes.EDIT_PROFILE)
                },
                onSettingsClick = {
                    navController.navigate(AppRoutes.SETTINGS)
                }
            )
        }

        // ============ JOB DETAIL ============
        composable(
            route = AppRoutes.JOB_DETAIL,
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(
                jobId = jobId,
                onBack = { navController.popBackStack() }
            )
        }

        // ============ PRICING ============
        composable(AppRoutes.PRICING) {
            PricingScreen()
        }

        // ============ EDIT PROFILE ============
        composable(AppRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // ============ SETTINGS ============
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}