package com.example.qualityapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.qualityapp.ui.navigation.Screen
import com.example.qualityapp.viewmodel.QuestionsViewModel

@Composable
fun QuestionsScreen(
    navController: NavHostController,
    questionnaireFileName: String,
    viewModel: QuestionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(questionnaireFileName) {
        viewModel.loadQuestionnaire(questionnaireFileName)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.error != null -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.retry(questionnaireFileName) },
                    onBack = { navController.popBackStack() }
                )

                state.questionnaire != null && state.currentQuestion != null -> {
                    QuestionContent(
                        state = state,
                        viewModel = viewModel,
                        onFinish = {
                            navController.navigate(
                                Screen.Finish.create(questionnaireFileName)
                            )
                        }
                    )
                }

                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("سوالی یافت نشد", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// -------------------------------------------------------------------

@Composable
private fun QuestionContent(
    state: com.example.qualityapp.viewmodel.QuestionsUiState,
    viewModel: QuestionsViewModel,
    onFinish: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val answer = state.answers[question.questionNumber]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ---- هدر: عنوان پرسشنامه + شماره سوال + نوار پیشرفت ----
        Text(
            text = state.questionnaire?.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "سوال ${state.currentIndex + 1} از ${state.total}",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1f) / state.total },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ---- بدنه قابل اسکرول: متن سوال + ۵ گزینه + توضیحات ----
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // متن سوال (بالا)
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            // ۵ گزینه (از فایل JSON خوانده می‌شوند)
            question.options.forEachIndexed { index, optionText ->
                val selected = answer?.selectedOptionIndex == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectOption(question.questionNumber, index)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = {
                            viewModel.selectOption(question.questionNumber, index)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // کادر توضیحات
            OutlinedTextField(
                value = answer?.comments.orEmpty(),
                onValueChange = { text ->
                    viewModel.updateComments(question.questionNumber, text)
                },
                label = { Text("توضیحات") },
                placeholder = { Text("در صورت نیاز توضیح اضافه کنید") },
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---- دکمه‌های قبلی / بعدی (یا پایان) ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = viewModel::previous,
                enabled = !state.isFirst,
                modifier = Modifier.weight(1f)
            ) {
                Text("قبلی")
            }

            Button(
                onClick = {
                    if (state.isLast) onFinish() else viewModel.next()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.isLast) "پایان" else "بعدی")
            }
        }
    }
}

// -------------------------------------------------------------------

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("تلاش مجدد")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("بازگشت")
        }
    }
}