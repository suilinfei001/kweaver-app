package com.kweaver.dip.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kweaver.dip.ui.screens.chat.ChatScreen
import com.kweaver.dip.ui.screens.digitalhuman.DigitalHumanDetailScreen
import com.kweaver.dip.ui.screens.digitalhuman.DigitalHumanEditScreen
import com.kweaver.dip.ui.screens.digitalhuman.DigitalHumanListScreen
import com.kweaver.dip.ui.screens.guide.GuideScreen
import com.kweaver.dip.ui.screens.home.HomeScreen
import com.kweaver.dip.ui.screens.login.LoginScreen
import com.kweaver.dip.ui.screens.login.LoginViewModel
import com.kweaver.dip.ui.screens.plans.PlanDetailScreen
import com.kweaver.dip.ui.screens.plans.PlanListScreen
import com.kweaver.dip.ui.screens.sessions.SessionDetailScreen
import com.kweaver.dip.ui.screens.sessions.SessionListScreen
import com.kweaver.dip.ui.screens.settings.SettingsScreen
import com.kweaver.dip.ui.screens.skills.SkillDetailScreen
import com.kweaver.dip.ui.screens.skills.SkillListScreen

@Composable
fun KWeaverNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val loginViewModel: LoginViewModel = hiltViewModel()
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) Route.Home.route else Route.Login.route

    val bottomBarTabs = listOf(
        Triple(Route.Home.route, "Chat", Icons.Default.Chat),
        Triple(Route.DigitalHumanList.route, "Agents", Icons.Default.SmartToy),
        Triple(Route.SessionList.route, "History", Icons.Default.History),
        Triple(Route.PlanList.route, "Plans", Icons.Default.Schedule)
    )

    val showBottomBar = bottomBarTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.first } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarTabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Route.Home.route) {
                            popUpTo(Route.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.Home.route) {
                HomeScreen(
                    onNavigateToChat = { agentId, sessionKey, agentName ->
                        navController.navigate(Route.Chat.createRoute(agentId, sessionKey, agentName))
                    },
                    onNavigateToSettings = { navController.navigate(Route.Settings.route) }
                )
            }

            composable(
                route = Route.Chat.route,
                arguments = listOf(
                    navArgument("agentId") { type = NavType.StringType },
                    navArgument("sessionKey") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("agentName") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val agentId = backStackEntry.arguments?.getString("agentId") ?: ""
                val sessionKey = backStackEntry.arguments?.getString("sessionKey")
                val agentName = backStackEntry.arguments?.getString("agentName")
                ChatScreen(
                    agentId = agentId,
                    sessionKey = sessionKey,
                    agentName = agentName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.DigitalHumanList.route) {
                DigitalHumanListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Route.DigitalHumanDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Route.DigitalHumanEdit.createRoute()) },
                    onNavigateToSkills = { navController.navigate(Route.SkillList.route) }
                )
            }

            composable(
                route = Route.DigitalHumanDetail.route,
                arguments = listOf(navArgument("digitalHumanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("digitalHumanId") ?: ""
                DigitalHumanDetailScreen(
                    digitalHumanId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId -> navController.navigate(Route.DigitalHumanEdit.createRoute(editId)) },
                    onNavigateToChat = { agentId, agentName ->
                        navController.navigate(Route.Chat.createRoute(agentId, agentName = agentName))
                    }
                )
            }

            composable(
                route = Route.DigitalHumanEdit.route,
                arguments = listOf(navArgument("digitalHumanId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("digitalHumanId")
                DigitalHumanEditScreen(
                    digitalHumanId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.SessionList.route) {
                SessionListScreen(
                    onNavigateToDetail = { key -> navController.navigate(Route.SessionDetail.createRoute(key)) }
                )
            }

            composable(
                route = Route.SessionDetail.route,
                arguments = listOf(navArgument("sessionKey") { type = NavType.StringType })
            ) { backStackEntry ->
                val key = backStackEntry.arguments?.getString("sessionKey") ?: ""
                SessionDetailScreen(
                    sessionKey = key,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.PlanList.route) {
                PlanListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Route.PlanDetail.createRoute(id)) }
                )
            }

            composable(
                route = Route.PlanDetail.route,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("planId") ?: ""
                PlanDetailScreen(
                    planId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.SkillList.route) {
                SkillListScreen(
                    onNavigateToDetail = { name -> navController.navigate(Route.SkillDetail.createRoute(name)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Route.SkillDetail.route,
                arguments = listOf(navArgument("skillName") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("skillName") ?: ""
                SkillDetailScreen(
                    skillName = name,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Route.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToGuide = { navController.navigate(Route.Guide.route) }
                )
            }

            composable(Route.Guide.route) {
                GuideScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
