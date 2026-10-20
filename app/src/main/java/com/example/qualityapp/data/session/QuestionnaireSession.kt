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

    private val answersMap = LinkedHashMap<Int, Answer>() // key = questionNumber

    /** شروع جلسه جدید. اگر فایل عوض شود، پاسخ‌های قبلی پاک می‌شوند. */
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

    /** پاک کردن کامل جلسه — برای دکمه «تکرار پرسشنامه». */
    fun reset() {
        fileName = null
        questionnaire = null
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

    /** برای نمایش در UI — یک کپی از پاسخ‌ها. */
    fun snapshot(): Map<Int, Answer> = answersMap.toMap()
}