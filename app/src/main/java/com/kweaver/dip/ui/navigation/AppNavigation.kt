package com.kweaver.dip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kweaver.dip.data.repository.AiConfigRepository
import com.kweaver.dip.ui.screens.chat.ChatScreen
import com.kweaver.dip.ui.screens.chat.ChatViewModel
import com.kweaver.dip.ui.screens.config.AiConfigScreen

@Composable
fun AppNavigation(
    hasConfig: Boolean,
) {
    val navController = rememberNavController()
    val startDestination = if (hasConfig) "chat" else "config"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("config") {
            AiConfigScreen(
                onConfigSaved = {
                    navController.navigate("chat") {
                        popUpTo("config") { inclusive = true }
                    }
                },
            )
        }
        composable("chat") {
            ChatScreen(
                onNavigateToSettings = {
                    navController.navigate("config")
                },
            )
        }
    }
}
