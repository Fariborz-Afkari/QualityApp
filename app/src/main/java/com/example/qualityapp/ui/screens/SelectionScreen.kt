package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen

@Composable
fun SelectionScreen(navController: NavHostController) {
    val lists = listOf(
        "بازرسی عمومی" to "general",
        "کنترل کیفیت محصول" to "product",
        "ایمنی محیط کار" to "safety"
    )
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("انتخاب چک‌لیست", style = MaterialTheme.typography.headlineMedium)
        Text("برای شروع، یکی از چک‌لیست‌های زیر را انتخاب کنید.")
        lists.forEach { (title, id) ->
            ElevatedCard(Modifier.fillMaxWidth(), onClick = { navController.navigate(Screen.Questions.create(id)) }) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text("شروع ←")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}
