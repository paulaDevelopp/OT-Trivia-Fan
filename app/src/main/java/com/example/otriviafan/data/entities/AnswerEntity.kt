package com.example.otriviafan.data.entities

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AnswerEntity(
    var id: Int = 0,
    var questionId: Int = 0,
    var answerText: String = ""
) {
    constructor() : this(0, 0, "")
}
