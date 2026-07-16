package com.ridelink.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ridelink.app.ui.home.HomeScreen
import com.ridelink.app.ui.otp.OtpScreen
import com.ridelink.app.ui.otp.PostLogin
import com.ridelink.app.ui.phone.PhoneScreen
import com.ridelink.app.ui.profile.ProfileSetupScreen
import com.ridelink.app.ui.startup.StartDestination
import com.ridelink.app.ui.startup.StartupScreen

object Routes {
    const val STARTUP = "startup"
    const val PHONE = "phone"
    const val OTP = "otp/{phone}?devCode={devCode}"
    const val PROFILE_SETUP = "profileSetup"
    const val HOME = "home"

    // phoneNumber is placed raw in the path segment; a literal '+' is valid there and is not
    // decoded to a space (that only happens in query strings).
    fun otp(phoneNumber: String, devCode: String?): String {
        val base = "otp/$phoneNumber"
        return if (devCode != null) "$base?devCode=$devCode" else base
    }
}

@Composable
fun AppNavHost(rootViewModel: RootViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // Forced logout (refresh failed): drop everything and return to phone login.
    LaunchedEffect(Unit) {
        rootViewModel.loggedOut.collect {
            navController.navigate(Routes.PHONE) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.STARTUP) {
        composable(Routes.STARTUP) {
            StartupScreen(onResolved = { dest ->
                val route = when (dest) {
                    StartDestination.HOME -> Routes.HOME
                    StartDestination.PROFILE_SETUP -> Routes.PROFILE_SETUP
                    StartDestination.PHONE -> Routes.PHONE
                }
                navController.navigate(route) {
                    popUpTo(Routes.STARTUP) { inclusive = true }
                }
            })
        }

        composable(Routes.PHONE) {
            PhoneScreen(onCodeSent = { phone, devCode ->
                navController.navigate(Routes.otp(phone, devCode))
            })
        }

        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("devCode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            OtpScreen(onVerified = { destination ->
                when (destination) {
                    PostLogin.HOME -> navController.toHome()
                    PostLogin.PROFILE_SETUP -> navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.PHONE) { inclusive = true }
                    }
                }
            })
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(onDone = { navController.toHome() })
        }

        composable(Routes.HOME) {
            HomeScreen(onLoggedOut = {
                navController.navigate(Routes.PHONE) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            })
        }
    }
}

private fun NavController.toHome() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}
