package com.example.qualityapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.repository.QuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppError(
    val title: String,
    val message: String,
    val details: String? = null
)

data class SelectionUiState(
    val isLoading: Boolean = false,
    val error: AppError? = null,
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

    fun onLineChange(v: String) = _uiState.update { it.copy(line = v, station = "") }
    fun onStationChange(v: String) = _uiState.update { it.copy(station = v) }
    fun onStationNumberChange(v: String) = _uiState.update { it.copy(stationNumber = v) }
    fun onTrainNumberChange(v: String) = _uiState.update { it.copy(trainNumber = v) }

    fun openQuestionnaireDialog() {
        _uiState.update { it.copy(showDialog = true) }
        if (_uiState.value.questionnaireNames.isEmpty()) loadQuestionnaireList()
    }

    fun dismissDialog() = _uiState.update { it.copy(showDialog = false) }

    fun selectQuestionnaire(name: String) =
        _uiState.update { it.copy(selectedName = name, showDialog = false) }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun loadQuestionnaireList() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val names = repository.listQuestionnaires()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questionnaireNames = names,
                        error = if (names.isEmpty())
                            AppError(
                                title = "لیست خالی است",
                                message = "هیچ فایل .json در پوشه questionnaires پیدا نشد."
                            )
                        else null
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.e("SelectionVM", "loadQuestionnaireList failed", t)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError(
                            title = "خطا در دریافت لیست",
                            message = t.message ?: t.javaClass.simpleName,
                            details = t.stackTraceToString().take(3000)
                        )
                    )
                }
            }
        }
    }
}