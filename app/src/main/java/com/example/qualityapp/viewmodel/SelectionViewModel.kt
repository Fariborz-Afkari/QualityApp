package com.example.qualityapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.repository.QuestionnaireRepository
import com.example.qualityapp.data.session.QuestionnaireSession
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
    private val repository: QuestionnaireRepository,
    private val session: QuestionnaireSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // اگر کاربر دکمه «تکرار پرسشنامه» را در صفحه پایان زده باشد،
        // همان پرسشنامه را از قبل انتخاب می‌کنیم و نام قبلی را نمایش می‌دهیم.
        session.lastSelectedFileName?.let { prev ->
            _uiState.update { it.copy(selectedName = prev) }
        }
    }

    // -------------------- Actions: فیلدهای ورودی --------------------

    fun onLineChange(v: String) {
        _uiState.update { it.copy(line = v, station = "") }
    }

    fun onStationChange(v: String) =
        _uiState.update { it.copy(station = v) }

    fun onStationNumberChange(v: String) =
        _uiState.update { it.copy(stationNumber = v) }

    fun onTrainNumberChange(v: String) =
        _uiState.update { it.copy(trainNumber = v) }

    // -------------------- Actions: دیالوگ پرسشنامه --------------------

    /** دکمه «انتخاب چک لیست» — دیالوگ باز می‌شود و اگر لیست خالی است، از گیت‌هاب می‌خواند. */
    fun openQuestionnaireDialog() {
        _uiState.update { it.copy(showDialog = true) }
        if (_uiState.value.questionnaireNames.isEmpty()) loadQuestionnaireList()
    }

    fun dismissDialog() =
        _uiState.update { it.copy(showDialog = false) }

    fun selectQuestionnaire(name: String) =
        _uiState.update { it.copy(selectedName = name, showDialog = false) }

    fun clearError() =
        _uiState.update { it.copy(error = null) }

    // -------------------- Actions: گرفتن لیست از گیت‌هاب --------------------

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

    // -------------------- Actions: شروع پرسشنامه --------------------

    /**
     * قبل از ناوبری به QuestionsScreen، متادیتای ورودی صفحه (خط، ایستگاه و ...)
     * را در session ذخیره می‌کند و پاسخ‌های قبلی را (اگر بوده) پاک می‌کند.
     *
     * @return true اگر پرسشنامه‌ای انتخاب شده باشد، وگرنه false.
     */
    fun startQuestionnaire(): Boolean {
        val name = _uiState.value.selectedName ?: return false

        session.setMetadata(
            line = _uiState.value.line,
            station = _uiState.value.station,
            stationNumber = _uiState.value.stationNumber,
            trainNumber = _uiState.value.trainNumber
        )
        session.resetAnswersKeepingQuestionnaire()
        session.lastSelectedFileName = name
        return true
    }

    /** برای دکمه «انصراف» در صفحه انتخاب — اگر session باز است ولی شروع نکرده، پاک شود. */
    fun cancelSelection() {
        // اگر پرسشنامه‌ای شروع نشده، فقط انتخاب را پاک کن
        if (session.questionnaire == null) {
            session.lastSelectedFileName = null
        }
    }
}