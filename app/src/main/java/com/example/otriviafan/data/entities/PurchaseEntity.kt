package com.example.otriviafan.data.entities

data class PurchaseEntity(
    val id: Int = 0, // si usás Firebase push(), también podés eliminar esto
    val userId: String = "",
    val storeItemId: String = "",
    val purchaseDate: Long = System.currentTimeMillis()
)
