package com.example.qualityapp.ui.navigation

import android.net.Uri
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
    object Reports : Screen("reports")

    object Questions : Screen("questions/{fileName}") {
        fun create(fileName: String) = "questions/${Uri.encode(fileName)}"
    }

    object Finish : Screen("finish/{fileName}") {
        fun create(fileName: String) = "finish/${Uri.encode(fileName)}"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Selection.route) { SelectionScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }

        composable(
            route = Screen.Questions.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { entry ->
            QuestionsScreen(
                navController = navController,
                questionnaireFileName = entry.arguments?.getString("fileName").orEmpty()
            )
        }

        composable(
            route = Screen.Finish.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { entry ->
            FinishScreen(
                navController = navController,
                questionnaireFileName = entry.arguments?.getString("fileName").orEmpty()
            )
        }
    }
}