package com.example.otriviafan.data.model
import com.example.otriviafan.data.entities.AnswerEntity
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuestionWithAnswers(
    var id: Int = 0,
    var questionText: String = "",
    var correctAnswerId: Int = 0,
    var answers: List<AnswerEntity> = emptyList(),
    var imageUrl: String = ""
) {
    constructor() : this(0, "", 0, emptyList(), "")
}
