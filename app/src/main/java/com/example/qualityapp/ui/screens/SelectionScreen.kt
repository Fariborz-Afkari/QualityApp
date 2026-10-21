package com.example.qualityapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.qualityapp.viewmodel.SelectionViewModel

// داده نمونه برای خطوط (می‌توانید بعداً از یک فایل JSON هم بخوانید)
private val METRO_LINES = listOf("خط ۱", "خط ۲", "خط ۳", "خط ۴", "خط ۵", "خط ۶", "خط ۷")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavHostController,
    viewModel: SelectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("انتخاب چک لیست") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- خط ----------
                DropdownField(
                    label = "خط",
                    value = state.line,
                    options = METRO_LINES,
                    onSelect = viewModel::onLineChange
                )
                Spacer(Modifier.height(12.dp))

                // ---------- ایستگاه ----------
                OutlinedTextField(
                    value = state.station,
                    onValueChange = viewModel::onStationChange,
                    label = { Text("ایستگاه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // ---------- شماره ایستگاه ----------
                OutlinedTextField(
                    value = state.stationNumber,
                    onValueChange = viewModel::onStationNumberChange,
                    label = { Text("شماره ایستگاه (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // ---------- شماره قطار ----------
                OutlinedTextField(
                    value = state.trainNumber,
                    onValueChange = viewModel::onTrainNumberChange,
                    label = { Text("شماره قطار (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // ---------- دکمه انتخاب چک لیست ----------
                Button(
                    onClick = { viewModel.openQuestionnaireDialog() },
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

                // ---------- کادر بزرگ نمایش چک لیست انتخاب‌شده ----------
                Spacer(Modifier.height(16.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.selectedName
                                ?: "چک لیستی انتخاب نشده است",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // ---------- نمایش خطا ----------
                state.error?.let { err ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.loadQuestionnaireList() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تلاش مجدد برای دریافت لیست") }
                }

                Spacer(Modifier.height(24.dp))

                // ---------- دکمه‌های شروع/انصراف ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) { Text("انصراف") }

                    Button(
                        onClick = {
                            val selected = state.selectedName ?: return@Button
                            navController.navigate(Screen.Questions.create(selected))
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }

                            state.questionnaireNames.isEmpty() -> Text(
                                state.error ?: "موردی یافت نشد"
                            )

                            else -> Column(
                                modifier = Modifier
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                state.questionnaireNames.forEach { name ->
                                    Text(
                                        text = name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectQuestionnaire(name)
                                            }
                                            .padding(vertical = 12.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (state.questionnaireNames.isEmpty() && !state.isLoading) {
                                viewModel.loadQuestionnaireList()
                            } else {
                                viewModel.dismissDialog()
                            }
                        }) {
                            Text(
                                if (state.questionnaireNames.isEmpty() && !state.isLoading)
                                    "تلاش مجدد"
                                else
                                    "بستن"
                            )
                        }
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------------
// Dropdown عمومی
// -------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}