package com.kweaver.dip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kweaver.dip.ui.screens.chat.ChatScreenPreview
import com.kweaver.dip.ui.screens.config.AiConfigScreen

@Composable
fun PreviewNavigation(
    hasConfig: Boolean,
) {
    val navController = rememberNavController()
    val startDestination = if (hasConfig) "chat_preview" else "config"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("config") {
            AiConfigScreen(
                onConfigSaved = {
                    navController.navigate("chat_preview") {
                        popUpTo("config") { inclusive = true }
                    }
                },
            )
        }
        composable("chat_preview") {
            ChatScreenPreview()
        }
    }
}

@Composable
fun ChatNavHost(
    hasConfig: Boolean,
    onNavigateToPreview: () -> Unit,
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
            ChatScreenPreview()
        }
    }
}