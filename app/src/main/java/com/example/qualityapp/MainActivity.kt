package com.example.qualityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.qualityapp.ui.navigation.AppNavHost
import com.example.qualityapp.ui.theme.QualityAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QualityAppTheme {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}