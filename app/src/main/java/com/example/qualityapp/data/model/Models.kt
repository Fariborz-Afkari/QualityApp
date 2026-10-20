package com.example.qualityapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Questionnaire(
    @Json(name = "title") val title: String,
    @Json(name = "questions") val questions: List<Question>
)

@JsonClass(generateAdapter = true)
data class Question(
    @Json(name = "questionNumber") val questionNumber: Int,
    @Json(name = "questionText") val questionText: String,
    @Json(name = "options") val options: List<String>
)

@JsonClass(generateAdapter = true)
data class Answer(
    @Json(name = "questionNumber") val questionNumber: Int,
    @Json(name = "questionText") val questionText: String,
    @Json(name = "selectedOptionIndex") val selectedOptionIndex: Int? = null,
    @Json(name = "selectedOptionText") val selectedOptionText: String? = null,
    @Json(name = "comments") val comments: String? = null
)

@JsonClass(generateAdapter = true)
data class Submission(
    @Json(name = "questionnaireTitle") val questionnaireTitle: String,
    @Json(name = "personnelName") val personnelName: String? = null,
    @Json(name = "personnelNumber") val personnelNumber: String? = null,
    @Json(name = "line") val line: String? = null,
    @Json(name = "station") val station: String? = null,
    @Json(name = "trainNumber") val trainNumber: String? = null,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "generalComments") val generalComments: String? = null,
    @Json(name = "answers") val answers: List<Answer>
)
