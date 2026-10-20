package com.example.qualityapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.model.Answer
import com.example.qualityapp.data.model.Question
import com.example.qualityapp.data.model.Questionnaire
import com.example.qualityapp.data.repository.QuestionnaireRepository
import com.example.qualityapp.data.session.QuestionnaireSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val questionnaire: Questionnaire? = null,
    val currentIndex: Int = 0,
    val answers: Map<Int, Answer> = emptyMap()
) {
    val total: Int get() = questionnaire?.questions?.size ?: 0
    val currentQuestion: Question? get() = questionnaire?.questions?.getOrNull(currentIndex)
    val isLast: Boolean get() = total > 0 && currentIndex == total - 1
    val isFirst: Boolean get() = currentIndex == 0
}

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val repository: QuestionnaireRepository,
    private val session: QuestionnaireSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionsUiState())
    val uiState = _uiState.asStateFlow()

    private var loadedFileName: String? = null

    fun loadQuestionnaire(fileName: String) {
        // اگر همین فایل قبلاً لود شده، فقط از session بازیابی می‌کنیم
        // (مثلاً پس از چرخش صفحه، ViewModel زنده می‌ماند و این شرط برقرار است)
        if (loadedFileName == fileName && session.questionnaire != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    questionnaire = session.questionnaire,
                    answers = session.snapshot(),
                    currentIndex = it.currentIndex.coerceIn(
                        0,
                        (session.questionnaire?.questions?.size ?: 1) - 1
                    )
                )
            }
            return
        }

        loadedFileName = fileName
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            runCatching { repository.loadQuestionnaire(fileName) }
                .onSuccess { q ->
                    session.start(fileName, q)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questionnaire = q,
                            currentIndex = 0,
                            answers = session.snapshot()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "خطا در خواندن پرسشنامه"
                        )
                    }
                }
        }
    }

    fun selectOption(questionNumber: Int, optionIndex: Int) {
        session.setOption(questionNumber, optionIndex)
        _uiState.update { it.copy(answers = session.snapshot()) }
    }

    fun updateComments(questionNumber: Int, text: String) {
        session.setComments(questionNumber, text)
        _uiState.update { it.copy(answers = session.snapshot()) }
    }

    fun next() {
        val max = _uiState.value.total - 1
        if (max < 0) return
        _uiState.update { it.copy(currentIndex = (it.currentIndex + 1).coerceAtMost(max)) }
    }

    fun previous() {
        _uiState.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0)) }
    }

    fun retry(fileName: String) {
        loadedFileName = null
        loadQuestionnaire(fileName)
    }
}