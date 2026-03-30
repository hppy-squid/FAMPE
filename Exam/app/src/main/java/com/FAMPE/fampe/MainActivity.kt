package com.FAMPE.fampe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.FAMPE.fampe.screen.AppScreen
import com.FAMPE.fampe.screen.BottomScreen
import com.FAMPE.fampe.service.PlayerNameDialog
import com.FAMPE.fampe.ui.theme.FampeTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            FampeTheme {
                var currentScreen by remember { mutableStateOf(BottomScreen.MAP) }

                PlayerNameDialog()

                AppScreen(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    }
}
