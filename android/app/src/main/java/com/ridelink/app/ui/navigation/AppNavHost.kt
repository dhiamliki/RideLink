package com.ridelink.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ridelink.app.ui.create.CreateOfferScreen
import com.ridelink.app.ui.create.CreateRequestScreen
import com.ridelink.app.ui.create.PostChooserScreen
import com.ridelink.app.ui.main.MainScreen
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
    const val MAIN = "main"
    const val POST_CHOOSER = "post"
    const val CREATE_OFFER = "createOffer"
    const val CREATE_REQUEST = "createRequest"

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
    // Which bottom-nav tab MainScreen shows; hoisted so create flows can steer it on return.
    var mainTab by rememberSaveable { mutableIntStateOf(0) }

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
                    StartDestination.HOME -> Routes.MAIN
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
                    PostLogin.HOME -> navController.toMain()
                    PostLogin.PROFILE_SETUP -> navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.PHONE) { inclusive = true }
                    }
                }
            })
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(onDone = { navController.toMain() })
        }

        composable(Routes.MAIN) {
            MainScreen(
                selectedTab = mainTab,
                onSelectTab = { mainTab = it },
                onPost = { navController.navigate(Routes.POST_CHOOSER) },
                onLoggedOut = {
                    navController.navigate(Routes.PHONE) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.POST_CHOOSER) {
            PostChooserScreen(
                onOfferRide = { navController.navigate(Routes.CREATE_OFFER) },
                onRequestRide = { navController.navigate(Routes.CREATE_REQUEST) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CREATE_OFFER) {
            CreateOfferScreen(
                onDone = {
                    mainTab = 0 // land on the feed, which reloads on entry
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CREATE_REQUEST) {
            CreateRequestScreen(
                onDone = {
                    mainTab = 1 // land on the requests tab
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun NavController.toMain() {
    navigate(Routes.MAIN) {
        popUpTo(graph.id) { inclusive = true }
    }
}
