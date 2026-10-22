package com.example.qualityapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen
import com.example.qualityapp.viewmodel.AppError
import com.example.qualityapp.viewmodel.SelectionViewModel
import androidx.compose.ui.text.font.FontFamily

private val METRO_LINES = listOf("خط ۱", "خط ۲", "خط ۳", "خط ۴", "خط ۵", "خط ۶", "خط ۷")

@Composable
fun SelectionScreen(
    navController: NavHostController,
    viewModel: SelectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("انتخاب چک لیست", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            var showDebug by remember { mutableStateOf(false) }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showDebug = true }) {
                    Text("🐞 دیباگ")
                }
            }

            if (showDebug) {
                DebugDialog(onDismiss = { showDebug = false })
            }

            SimpleDropdown("خط", state.line, METRO_LINES, viewModel::onLineChange)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                state.station, viewModel::onStationChange,
                label = { Text("ایستگاه") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                state.stationNumber, viewModel::onStationNumberChange,
                label = { Text("شماره ایستگاه (اختیاری)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                state.trainNumber, viewModel::onTrainNumberChange,
                label = { Text("شماره قطار (اختیاری)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = viewModel::openQuestionnaireDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("انتخاب چک لیست")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedCard(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().height(120.dp).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.selectedName ?: "چک لیستی انتخاب نشده",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) { Text("انصراف") }
                Button(
                    onClick = {
                        state.selectedName?.let {
                            navController.navigate(Screen.Questions.create(it))
                        }
                    },
                    enabled = state.selectedName != null,
                    modifier = Modifier.weight(1f)
                ) { Text("شروع") }
            }
        }

        // ---------- دیالوگ انتخاب پرسشنامه ----------
        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDialog,
                title = { Text("پرسشنامه‌ها") },
                text = {
                    when {
                        state.isLoading -> Box(
                            Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }

                        state.questionnaireNames.isEmpty() && state.error == null ->
                            Text("موردی یافت نشد")

                        state.error != null ->
                            Text(state.error!!.message, color = MaterialTheme.colorScheme.error)

                        else -> Column(
                            Modifier.heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            state.questionnaireNames.forEach { name ->
                                Text(
                                    name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectQuestionnaire(name) }
                                        .padding(vertical = 12.dp)
                                )
                                Spacer(Modifier.height(1.dp).fillMaxWidth())
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (state.error != null) {
                            viewModel.clearError()
                            viewModel.loadQuestionnaireList()
                        } else viewModel.dismissDialog()
                    }) {
                        Text(if (state.error != null) "تلاش مجدد" else "بستن")
                    }
                },
                dismissButton = if (state.error != null) {
                    { TextButton(onClick = viewModel::clearError) { Text("انصراف") } }
                } else null
            )
        }

        // ---------- دیالوگ نمایش خطای دقیق ----------
        state.error?.let { err ->
            ErrorDialog(
                error = err,
                onRetry = { viewModel.loadQuestionnaireList() },
                onDismiss = viewModel::clearError
            )
        }
    }
}
//--------------------------
@Composable
private fun DebugDialog(onDismiss: () -> Unit) {
    val logs by com.example.qualityapp.data.DebugLog.lines.collectAsStateWithLifecycle()
    val clipboard = androidx.compose.ui.platform.LocalContext.current
        .getSystemService(android.content.ClipboardManager::class.java)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("لاگ دیباگ (${logs.size} خط)") },
        text = {
            if (logs.isEmpty()) {
                Text("هنوز لاگی نیست. دکمه «انتخاب چک لیست» را بزنید.")
            } else {
                Column(
                    Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    logs.forEach { line ->
                        Text(
                            line,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard?.setPrimaryClip(
                    android.content.ClipData.newPlainText("debug", logs.joinToString("\n"))
                )
            }) { Text("کپی همه") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    com.example.qualityapp.data.DebugLog.clear()
                }) { Text("پاک کردن") }
                TextButton(onClick = onDismiss) { Text("بستن") }
            }
        }
    )
}
// -------------------------------------------------------------------
@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { onSelect(opt); expanded = false }
                )
            }
        }
    }
}

// -------------------------------------------------------------------
@Composable
private fun ErrorDialog(
    error: AppError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(error.title, color = MaterialTheme.colorScheme.error) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(error.message)
                if (error.details != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Text(if (showDetails) "پنهان کردن جزئیات" else "نمایش جزئیات فنی")
                    }
                    if (showDetails) {
                        Text(
                            error.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRetry) { Text("تلاش مجدد") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}