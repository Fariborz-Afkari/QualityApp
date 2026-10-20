package com.example.qualityapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qualityapp.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Selection : Screen("selection")
    object Questions : Screen("questions/{id}") {
        fun create(id: String) = "questions/$id"
    }
    object Finish : Screen("finish/{id}") {
        fun create(id: String) = "finish/$id"
    }
    object Reports : Screen("reports")
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Selection.route) { SelectionScreen(navController) }
        composable(
            Screen.Questions.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            QuestionsScreen(navController, entry.arguments?.getString("id") ?: "")
        }
        composable(
            Screen.Finish.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            FinishScreen(navController, entry.arguments?.getString("id") ?: "")
        }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
    }
}