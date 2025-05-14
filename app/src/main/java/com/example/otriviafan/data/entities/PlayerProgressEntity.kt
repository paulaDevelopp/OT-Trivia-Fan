package com.example.otriviafan.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayerProgressEntity(
    @PrimaryKey val userId: String,
    val nivelesCompletados: List<Int>
)
