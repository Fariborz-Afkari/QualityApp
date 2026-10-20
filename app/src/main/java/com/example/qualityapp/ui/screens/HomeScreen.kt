package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("کیفیت", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))
        Button(onClick = { navController.navigate(Screen.Selection.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("انتخاب چک لیست")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate(Screen.Settings.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("تنظیمات")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate(Screen.Reports.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("گزارشات")
        }
    }
}