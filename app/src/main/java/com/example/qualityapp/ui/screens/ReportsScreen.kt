package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ReportsScreen(navController: NavHostController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("گزارشات", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("در نسخه بعدی، لیست پاسخ‌های ذخیره‌شده اینجا نمایش داده می‌شود.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) { Text("بازگشت") }
    }
}