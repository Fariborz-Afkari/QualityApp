package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.qualityapp.viewmodel.FinishViewModel

@Composable
fun FinishScreen(
    navController: NavHostController,
    questionnaireFileName: String,
    viewModel: FinishViewModel = hiltViewModel()
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("صفحه پایان — در مرحله بعد تکمیل می‌شود",
                style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("پاسخ‌های ثبت‌شده: ${viewModel.session.snapshot().size}",
                style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("بازگشت")
            }
        }
    }
}