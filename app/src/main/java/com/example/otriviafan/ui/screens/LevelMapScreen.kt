package com.example.otriviafan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.UserViewModel
import kotlinx.coroutines.launch

enum class TipoNivel { INDIVIDUAL, MULTIJUGADOR }

data class NivelUI(
    val id: Int,
    val tipo: TipoNivel,
    val desbloqueado: Boolean,
    val completado: Boolean
)

@Composable
fun LevelMapScreen(navController: NavController, userViewModel: UserViewModel) {
    val scope = rememberCoroutineScope()
    val repository = remember { Repository() }
    var niveles by remember { mutableStateOf<List<NivelUI>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userId = userViewModel.getUserId()
            val progreso = userViewModel.getNivelProgreso(userId)

            niveles = (1..20).map { id ->
                val tipo = if (id % 4 == 0) TipoNivel.MULTIJUGADOR else TipoNivel.INDIVIDUAL
                val completado = progreso[id]?.completado == true

                val docName = nombreDocDelNivel(id)
                val requiresMultiplayer = if (tipo == TipoNivel.INDIVIDUAL) {
                    repository.isMultiplayerRequiredForLevel(docName)
                } else false

                val desbloqueado = when {
                    id == 1 -> true
                    tipo == TipoNivel.INDIVIDUAL -> {
                        val prevCompleted = progreso[id - 1]?.completado == true
                        val multijugadorPrevioGanado = progreso[id - 1]?.tipo == "multijugador" && progreso[id - 1]?.completado == true
                        (!requiresMultiplayer && prevCompleted) || (requiresMultiplayer && multijugadorPrevioGanado)
                    }
                    tipo == TipoNivel.MULTIJUGADOR -> (id - 1 downTo id - 3).all { progreso[it]?.completado == true }
                    else -> false
                }

                NivelUI(id, tipo, desbloqueado, completado)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mapa de niveles", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        niveles.chunked(4).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                fila.forEach { nivel ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    when {
                                        nivel.tipo == TipoNivel.MULTIJUGADOR -> if (nivel.completado) Color(0xFF1976D2) else if (nivel.desbloqueado) Color(0xFF64B5F6) else Color.LightGray
                                        else -> if (nivel.completado) Color(0xFF388E3C) else if (nivel.desbloqueado) Color(0xFF81C784) else Color.Gray
                                    },
                                    shape = CircleShape
                                )
                                .clickable(enabled = nivel.desbloqueado) {
                                    val route = if (nivel.tipo == TipoNivel.INDIVIDUAL)
                                        "${Screen.SinglePlayer.route}/${nivel.id}"
                                    else
                                        "multiplayer_game_screen/${nivel.id}"
                                    navController.navigate(route)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nivel.id.toString(), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (nivel.tipo == TipoNivel.MULTIJUGADOR) "🧑‍🤝‍🧑" else "🎯",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun nombreDocDelNivel(id: Int): String {
    return when (id) {
        in 1..3 -> "easy_level$id"
        in 4..6 -> "medium_level${id - 3}"
        in 7..9 -> "difficult_level${id - 6}"
        in 10..20 -> "difficult_level${id - 6}"
        else -> "easy_level1"
    }
}
