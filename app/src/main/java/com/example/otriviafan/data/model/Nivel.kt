package com.example.otriviafan.data.model

enum class TipoNivel { INDIVIDUAL, MULTIJUGADOR }

data class NivelUI(
    val id: Int,
    val tipo: TipoNivel,
    val desbloqueado: Boolean,
    val completado: Boolean
)
