package com.kweaver.dip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kweaver.dip.ui.screens.chat.ChatScreen
import com.kweaver.dip.ui.screens.chat.ChatScreenPreview
import com.kweaver.dip.ui.screens.chat.ChatScreenVariant
import com.kweaver.dip.ui.screens.config.AiConfigScreen

@Composable
fun PreviewNavigation(
    hasConfig: Boolean,
    initialVariant: ChatScreenVariant = ChatScreenVariant.ORIGINAL,
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
            ChatScreenPreview(
                onNavigateToSettings = {
                    navController.navigate("config")
                },
                initialVariant = initialVariant,
            )
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
            ChatScreen(
                onNavigateToSettings = {
                    navController.navigate("config")
                },
            )
        }
        composable("chat_preview") {
            ChatScreenPreview(
                onNavigateToSettings = {
                    navController.navigate("config")
                },
                initialVariant = ChatScreenVariant.ORIGINAL,
            )
        }
    }
}