package com.example.otriviafan.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Match(
    var matchId: String = "",
    var player1Id: String = "",
    var player2Id: String? = null,
    var player1Score: Int = 0,
    var player2Score: Int = 0,
    var currentQuestionIndex: Int = 0,
    var currentWinner: String? = null,
    var answered: Map<String, Boolean> = emptyMap(),
    var questions: List<QuestionWithAnswers> = emptyList(),
    var status: String = "waiting",
    var difficulty: String = "",
    var levelName: String = ""
) {
    constructor() : this("", "", null, 0, 0, 0, null, emptyMap(), emptyList(), "waiting", "", "")
}
