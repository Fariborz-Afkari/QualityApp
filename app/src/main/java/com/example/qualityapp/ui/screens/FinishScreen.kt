package com.example.qualityapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen
import com.example.qualityapp.viewmodel.FinishViewModel

@Composable
fun FinishScreen(
    navController: NavHostController,
    questionnaireFileName: String,
    viewModel: FinishViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "پایان پرسشنامه",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                viewModel.session.questionnaire?.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // ---- خلاصه پاسخ‌ها ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "تعداد سوالات: ${state.totalQuestions}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "پاسخ‌داده‌نشده: ${state.unansweredCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.unansweredCount > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    if (viewModel.session.line.isNotBlank() ||
                        viewModel.session.station.isNotBlank() ||
                        viewModel.session.trainNumber.isNotBlank()
                    ) {
                        Spacer(Modifier.height(6.dp))
                        if (viewModel.session.line.isNotBlank())
                            Text("خط: ${viewModel.session.line}",
                                style = MaterialTheme.typography.bodySmall)
                        if (viewModel.session.station.isNotBlank())
                            Text("ایستگاه: ${viewModel.session.station}",
                                style = MaterialTheme.typography.bodySmall)
                        if (viewModel.session.stationNumber.isNotBlank())
                            Text("شماره ایستگاه: ${viewModel.session.stationNumber}",
                                style = MaterialTheme.typography.bodySmall)
                        if (viewModel.session.trainNumber.isNotBlank())
                            Text("شماره قطار: ${viewModel.session.trainNumber}",
                                style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- کادر توضیحات نهایی ----
            OutlinedTextField(
                value = state.generalComments,
                onValueChange = viewModel::onCommentsChange,
                label = { Text("توضیحات نهایی") },
                placeholder = { Text("در صورت نیاز توضیح کلی اضافه کنید") },
                minLines = 3,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.savedResult == null
            )

            Spacer(Modifier.height(16.dp))

            // ---- نام فایل پیش‌نمایش ----
            if (state.previewFileName.isNotBlank()) {
                Text("نام فایل ذخیره‌شده:",
                    style = MaterialTheme.typography.labelMedium)
                Text(
                    state.previewFileName,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(16.dp))
            }

            // ---- نمایش پیام ذخیره موفق ----
            if (state.savedResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "✅ با موفقیت در گیت‌هاب ذخیره شد",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "مسیر: ${state.savedResult!!.filePath}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                        state.savedResult!!.commitSha?.let { sha ->
                            Text(
                                "Commit: ${sha.take(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- نمایش خطا ----
            state.error?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "خطا در ذخیره",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- دکمه ذخیره ----
            if (state.savedResult == null) {
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.isSaving) "در حال ذخیره..." else "ذخیره در گیت‌هاب")
                }
            } else {
                // ---- بعد از ذخیره موفق: دو دکمه ----
                Button(
                    onClick = {
                        viewModel.resetForRepeat()
                        navController.navigate(Screen.Selection.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تکرار پرسشنامه")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.resetForNew()
                        navController.navigate(Screen.Selection.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("پرسشنامه جدید")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- بازگشت (فقط اگر ذخیره نشده) ----
            if (state.savedResult == null) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("بازگشت") }
            }
        }
    }
}