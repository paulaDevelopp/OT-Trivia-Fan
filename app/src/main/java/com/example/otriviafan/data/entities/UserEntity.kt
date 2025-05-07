package com.example.otriviafan.data.entities

data class UserEntity(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val points: Int = 0,
    val highestLevelUnlocked: Int = 1, // 👈 NUEVO campo para guardar el nivel más alto desbloqueado
    val createdAt: Long = 0L
)
