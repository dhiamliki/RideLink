package com.ridelink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.ridelink.app.ui.navigation.AppNavHost
import com.ridelink.app.ui.theme.RideLinkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RideLinkTheme {
                Surface {
                    AppNavHost()
                }
            }
        }
    }
}
