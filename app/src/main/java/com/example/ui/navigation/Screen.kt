package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Primary Bottom Navigation Destinations
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Goals : Screen("goals", "My Goal", Icons.Filled.Flag)
    object Today : Screen("today", "Today", Icons.Filled.CheckCircle)
    object Roadmap : Screen("roadmap", "Roadmap", Icons.Filled.Map)
    object Coach : Screen("coach", "AI Coach", Icons.Filled.AutoAwesome)
    object More : Screen("more", "More", Icons.Filled.MoreHoriz)

    // Secondary Destinations Accessible from More or Details
    object LearningHub : Screen("learning_hub", "Learning Hub")
    object Notes : Screen("notes", "Notes")
    object Progress : Screen("progress", "Progress & Reviews")
    object Hackathon : Screen("hackathon", "Hackathon Intelligence")
    object Business : Screen("business", "Business Intelligence")
    object Memory : Screen("memory", "Personal Memory")
    object Explore : Screen("explore", "Resource Discovery")
    object Auth : Screen("auth", "Account & Cloud Sync")
    object Settings : Screen("settings", "Settings")

    companion object {
        val bottomNavItems = listOf(Home, Goals, Today, Roadmap, Coach, More)
    }
}
