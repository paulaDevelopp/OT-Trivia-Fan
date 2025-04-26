package com.example.otriviafan.data.entities

data class PlayerProgressEntity(
    val id: Int = 0, // no se necesita si usás Firebase push(), podés quitarlo si querés
    val userId: String = "",
    val questionId: Int = 0,
    val correct: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
