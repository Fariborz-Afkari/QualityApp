package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen

private val checklistQuestions = listOf(
    "محیط کار تمیز و مرتب است؟",
    "ابزار و تجهیزات سالم هستند؟",
    "استانداردهای کیفیت رعایت شده‌اند؟",
    "موارد ایمنی بررسی شده‌اند؟",
    "مستندات لازم در دسترس هستند؟"
)

@Composable
fun QuestionsScreen(navController: NavHostController, id: String) {
    val answers = remember(id) { mutableStateListOf<Boolean?>().apply { repeat(checklistQuestions.size) { add(null) } } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("ارزیابی کیفیت", style = MaterialTheme.typography.headlineMedium)
        Text("چک‌لیست: $id")
        checklistQuestions.forEachIndexed { index, question ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("${index + 1}. $question", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = answers[index] == true, onClick = { answers[index] = true }, label = { Text("بله") })
                        FilterChip(selected = answers[index] == false, onClick = { answers[index] = false }, label = { Text("خیر") })
                    }
                }
            }
        }
        Button(
            onClick = { navController.navigate(Screen.Finish.create(id)) },
            enabled = answers.all { it != null },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ثبت و پایان ارزیابی") }
        TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}
