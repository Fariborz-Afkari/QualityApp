package com.example.qualityapp.data.session

import com.example.qualityapp.data.model.Answer
import com.example.qualityapp.data.model.Questionnaire
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireSession @Inject constructor() {

    var fileName: String? = null
        private set
    var questionnaire: Questionnaire? = null
        private set

    // اطلاعات ایستگاه که در صفحه انتخاب وارد می‌شود
    var line: String = ""
        private set
    var station: String = ""
        private set
    var stationNumber: String = ""
        private set
    var trainNumber: String = ""
        private set

    /** برای پیش‌انتخاب در صفحه انتخاب هنگام «تکرار پرسشنامه» */
    var lastSelectedFileName: String? = null

    private val answersMap = LinkedHashMap<Int, Answer>()

    /** تنظیم متادیتا — از SelectionViewModel فراخوانی می‌شود. */
    fun setMetadata(
        line: String,
        station: String,
        stationNumber: String,
        trainNumber: String
    ) {
        this.line = line.trim()
        this.station = station.trim()
        this.stationNumber = stationNumber.trim()
        this.trainNumber = trainNumber.trim()
    }

    /** شروع پرسشنامه — فقط اگر فایل عوض شود، پاسخ‌ها را پاک می‌کند. */
    fun start(fileName: String, questionnaire: Questionnaire) {
        if (this.fileName != fileName) {
            this.fileName = fileName
            this.questionnaire = questionnaire
            answersMap.clear()
            questionnaire.questions.forEach { q ->
                answersMap[q.questionNumber] = Answer(
                    questionNumber = q.questionNumber,
                    questionText = q.questionText
                )
            }
        }
    }

    /** پاک‌کردن پاسخ‌ها برای «تکرار پرسشنامه» — پرسشنامه و متادیتا باقی می‌مانند. */
    fun resetAnswersKeepingQuestionnaire() {
        val q = questionnaire ?: return
        answersMap.clear()
        q.questions.forEach { qq ->
            answersMap[qq.questionNumber] = Answer(qq.questionNumber, qq.questionText)
        }
    }

    /** پاک‌کردن کل session — برای «پرسشنامه جدید». */
    fun reset() {
        fileName = null
        questionnaire = null
        line = ""
        station = ""
        stationNumber = ""
        trainNumber = ""
        lastSelectedFileName = null
        answersMap.clear()
    }

    fun setOption(questionNumber: Int, optionIndex: Int) {
        val q = questionnaire?.questions?.firstOrNull { it.questionNumber == questionNumber } ?: return
        val optionText = q.options.getOrNull(optionIndex) ?: return
        val current = answersMap[questionNumber]
            ?: Answer(questionNumber, q.questionText)
        answersMap[questionNumber] = current.copy(
            selectedOptionIndex = optionIndex,
            selectedOptionText = optionText
        )
    }

    fun setComments(questionNumber: Int, text: String) {
        val current = answersMap[questionNumber] ?: return
        answersMap[questionNumber] = current.copy(
            comments = text.takeIf { it.isNotBlank() }
        )
    }

    fun snapshot(): Map<Int, Answer> = answersMap.toMap()
}