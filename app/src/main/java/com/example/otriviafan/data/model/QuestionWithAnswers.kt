package com.example.otriviafan.data.model
import com.example.otriviafan.data.entities.AnswerEntity
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuestionWithAnswers(
    val id: Int = 0,
    val questionText: String = "",
    val correctAnswerId: Int = 0,
    val imageUrl: String = "",
    val answers: List<AnswerEntity> = emptyList()
)
{   constructor() : this(0, "", 0, "", emptyList())

}
