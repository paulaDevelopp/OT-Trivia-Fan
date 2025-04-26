package com.example.otriviafan.data.entities

data class UserEntity(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val points: Int = 0,
    val createdAt: Long = 0L
)
