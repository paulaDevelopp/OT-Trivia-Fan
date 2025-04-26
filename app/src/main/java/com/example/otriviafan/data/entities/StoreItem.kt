package com.example.otriviafan.data.entities

data class StoreItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val type: String = "" // "sticker" o "background"
)
