package com.example.qualityapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.repository.QuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val questionnaireNames: List<String> = emptyList(),
    val selectedName: String? = null,
    val showDialog: Boolean = false,
    val line: String = "",
    val station: String = "",
    val stationNumber: String = "",
    val trainNumber: String = ""
)

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectionUiState())
    val uiState = _uiState.asStateFlow()

    // ------------------ Actions ------------------

    fun onLineChange(v: String) {
        _uiState.update { it.copy(line = v, station = "") }
    }
    fun onStationChange(v: String) = _uiState.update { it.copy(station = v) }
    fun onStationNumberChange(v: String) = _uiState.update { it.copy(stationNumber = v) }
    fun onTrainNumberChange(v: String) = _uiState.update { it.copy(trainNumber = v) }

    /** کلیک روی دکمه "انتخاب چک لیست" — این متد باید لیست را از گیت‌هاب بخواند. */
    fun openQuestionnaireDialog() {
        _uiState.update { it.copy(showDialog = true, error = null) }
        // اگر قبلاً لیست گرفته نشده، از گیت‌هاب بگیر
        if (_uiState.value.questionnaireNames.isEmpty()) {
            loadQuestionnaireList()
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun selectQuestionnaire(name: String) {
        _uiState.update { it.copy(selectedName = name, showDialog = false) }
    }

    fun loadQuestionnaireList() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.listQuestionnaires() }
                .onSuccess { names ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questionnaireNames = names,
                            error = if (names.isEmpty())
                                "هیچ فایل .json در پوشه questionnaires پیدا نشد"
                            else null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "خطا در ارتباط با گیت‌هاب"
                        )
                    }
                }
        }
    }
}