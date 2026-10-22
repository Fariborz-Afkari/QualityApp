package com.example.qualityapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualityapp.data.model.Submission
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import com.example.qualityapp.data.repository.QuestionnaireRepository
import com.example.qualityapp.data.session.QuestionnaireSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SavedResult(
    val fileName: String,
    val filePath: String,
    val commitSha: String?,
    val commitUrl: String?
)

data class FinishUiState(
    val personnelName: String = "",
    val personnelNumber: String = "",
    val generalComments: String = "",
    val previewFileName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedResult: SavedResult? = null,
    val unansweredCount: Int = 0,
    val totalQuestions: Int = 0
)

@HiltViewModel
class FinishViewModel @Inject constructor(
    val session: QuestionnaireSession,
    private val repository: QuestionnaireRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinishUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // آمار پاسخ‌ها را بگیر
        val answers = session.snapshot()
        val total = answers.size
        val unanswered = answers.values.count { it.selectedOptionIndex == null }
        _uiState.update {
            it.copy(totalQuestions = total, unansweredCount = unanswered)
        }

        // نام و شماره پرسنلی از تنظیمات
        viewModelScope.launch {
            combine(prefs.personnelName, prefs.personnelNumber) { n, num -> n to num }
                .collect { (n, num) ->
                    _uiState.update {
                        it.copy(
                            personnelName = n,
                            personnelNumber = num,
                            previewFileName = buildFileName(n)
                        )
                    }
                }
        }
    }

    fun onCommentsChange(v: String) =
        _uiState.update { it.copy(generalComments = v, error = null) }

    fun save() {
        if (_uiState.value.isSaving) return
        if (_uiState.value.savedResult != null) return

        val q = session.questionnaire
        if (q == null) {
            _uiState.update { it.copy(error = "پرسشنامه‌ای بارگذاری نشده است") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val fileName = buildFileName(_uiState.value.personnelName)
                val timestamp = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.US
                ).format(Date())

                val submission = Submission(
                    questionnaireTitle = q.title,
                    personnelName     = _uiState.value.personnelName.trim().ifBlank { null },
                    personnelNumber   = _uiState.value.personnelNumber.trim().ifBlank { null },
                    line              = session.line.ifBlank { null },
                    station           = session.station.ifBlank { null },
                    stationNumber     = session.stationNumber.ifBlank { null },
                    trainNumber       = session.trainNumber.ifBlank { null },
                    timestamp         = timestamp,
                    generalComments   = _uiState.value.generalComments.trim().ifBlank { null },
                    answers           = session.snapshot().values.toList()
                )

                val result = repository.saveSubmission(fileName, submission)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedResult = SavedResult(
                            fileName = fileName,
                            filePath = result.filePath,
                            commitSha = result.commitSha,
                            commitUrl = result.commitUrl
                        )
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = t.message ?: "خطا در ذخیره‌سازی"
                    )
                }
            }
        }
    }

    /** برای دکمه «تکرار پرسشنامه» */
    fun resetForRepeat() {
        session.resetAnswersKeepingQuestionnaire()
    }

    /** برای دکمه «پرسشنامه جدید» */
    fun resetForNew() {
        session.reset()
    }

    /**
     * ساخت نام فایل طبق الگو:
     * [عنوان پرسشنامه]_[خط]_[نام پرسنل]_[تاریخ_ساعت].json
     * کاراکترهای غیرمجاز و فاصله‌ها با «-» جایگزین می‌شوند.
     */
    private fun buildFileName(personnelName: String): String {
        val title = session.questionnaire?.title ?: "questionnaire"
        val line = session.line.ifBlank { "no-line" }
        val name = personnelName.ifBlank { "no-name" }
        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss", Locale.US
        ).format(Date())

        val raw = "${title}_${line}_${name}_$timestamp"

        return raw
            .replace(Regex("[\\\\/:*?\"<>|]+"), "-")   // کاراکترهای غیرمجاز
            .replace(Regex("\\s+"), "-")                // فاصله‌ها
            .replace(Regex("-+"), "-")                   // دش‌های پشت‌سرهم
            .trim('-')
            .take(200)                                   // محدودیت طول
            .let { "$it.json" }
    }
}