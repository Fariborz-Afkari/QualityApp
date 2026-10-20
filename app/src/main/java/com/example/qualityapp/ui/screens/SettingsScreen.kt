package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(navController: NavHostController) {
    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("فعال بودن اعلان‌ها")
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("حالت تیره (نمایشی)")
            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
        }
        Text("نسخه ۱.۰", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}
