package com.example.otriviafan.data.model

enum class TipoNivel {
    INDIVIDUAL, MULTIJUGADOR
}

data class Nivel(
    val id: Int,
    val tipo: TipoNivel,
    var desbloqueado: Boolean = false,
    var completado: Boolean = false
)
