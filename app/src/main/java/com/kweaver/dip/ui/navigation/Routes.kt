package com.kweaver.dip.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Home : Route("home")
    data object AppStore : Route("app-store")
    data object Chat : Route("chat/{agentId}?sessionKey={sessionKey}&agentName={agentName}") {
        fun createRoute(agentId: String, sessionKey: String? = null, agentName: String? = null): String {
            val sb = StringBuilder("chat/$agentId")
            val params = mutableListOf<String>()
            sessionKey?.let { params.add("sessionKey=$it") }
            agentName?.let { params.add("agentName=$it") }
            if (params.isNotEmpty()) sb.append("?").append(params.joinToString("&"))
            return sb.toString()
        }
    }
    data object DigitalHumanList : Route("digital-human")
    data object DigitalHumanDetail : Route("digital-human/{digitalHumanId}") {
        fun createRoute(id: String) = "digital-human/$id"
    }
    data object DigitalHumanEdit : Route("digital-human/edit?digitalHumanId={digitalHumanId}") {
        fun createRoute(id: String? = null) = if (id != null) "digital-human/edit?digitalHumanId=$id" else "digital-human/edit"
    }
    data object SessionList : Route("sessions")
    data object SessionDetail : Route("sessions/{sessionKey}") {
        fun createRoute(key: String) = "sessions/$key"
    }
    data object PlanList : Route("plans")
    data object PlanDetail : Route("plans/{planId}") {
        fun createRoute(id: String) = "plans/$id"
    }
    data object SkillList : Route("skills")
    data object SkillDetail : Route("skills/{skillName}") {
        fun createRoute(name: String) = "skills/$name"
    }
    data object Settings : Route("settings")
    data object Guide : Route("guide")
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Route.Home.route, "Chat", Icons.AutoMirrored.Filled.Chat),
    BottomNavItem(Route.DigitalHumanList.route, "Agents", Icons.Filled.SmartToy),
    BottomNavItem(Route.AppStore.route, "Store", Icons.Filled.Apps),
    BottomNavItem(Route.SessionList.route, "History", Icons.Filled.History),
    BottomNavItem(Route.PlanList.route, "Plans", Icons.Filled.Schedule)
)
