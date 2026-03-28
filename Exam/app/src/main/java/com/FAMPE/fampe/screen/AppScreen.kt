package com.FAMPE.fampe.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScreen(
    currentScreen: BottomScreen,
    onScreenSelected: (BottomScreen) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == BottomScreen.MAP,
                    onClick = { onScreenSelected(BottomScreen.MAP) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Map") },
                    label = { Text("Map") }
                )

                NavigationBarItem(
                    selected = currentScreen == BottomScreen.LEADERBOARD,
                    onClick = { onScreenSelected(BottomScreen.LEADERBOARD) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") }
                )

                NavigationBarItem(
                    selected = currentScreen == BottomScreen.USER,
                    onClick = { onScreenSelected(BottomScreen.USER) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "User") },
                    label = { Text("User") }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            BottomScreen.MAP -> {
                MapScreen(modifier = Modifier.padding(innerPadding))
            }
            BottomScreen.LEADERBOARD -> {
                LeaderboardScreen(modifier = Modifier.padding(innerPadding))
            }
            BottomScreen.USER -> {
                UserScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}