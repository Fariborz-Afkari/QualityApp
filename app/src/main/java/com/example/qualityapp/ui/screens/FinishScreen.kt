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
fun FinishScreen(navController: NavHostController, id: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ارزیابی به پایان رسید", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("چک‌لیست «$id» تکمیل شد.")
        Spacer(Modifier.height(28.dp))
        Button(onClick = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("بازگشت به صفحه اصلی") }
        OutlinedButton(onClick = { navController.navigate(Screen.Reports.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("مشاهده گزارش‌ها")
        }
    }
}
