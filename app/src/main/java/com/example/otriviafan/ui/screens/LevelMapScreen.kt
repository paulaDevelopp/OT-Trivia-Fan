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
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import com.example.otriviafan.R

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
    var niveles by remember { mutableStateOf<List<NivelUI>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userId = userViewModel.getUserId()
            val progreso = userViewModel.getNivelProgreso(userId)

            // Solo 10 niveles
            niveles = (1..10).map { id ->
                val tipo = if (isMultiplayerLevel(id)) TipoNivel.MULTIJUGADOR else TipoNivel.INDIVIDUAL
                val completado = progreso[id]?.completado == true
                val desbloqueado = when (id) {
                    1 -> true
                    else -> progreso[id - 1]?.completado == true
                }
                NivelUI(id, tipo, desbloqueado, completado)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.height(1500.dp)) {
            Image(
                painter = painterResource(id = R.drawable.fondo_niveles),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            val posiciones = listOf(
                DpOffset(100.dp, 80.dp),
                DpOffset(200.dp, 180.dp),
                DpOffset(80.dp, 280.dp),
                DpOffset(220.dp, 400.dp),
                DpOffset(110.dp, 500.dp),
                DpOffset(180.dp, 620.dp),
                DpOffset(100.dp, 720.dp),
                DpOffset(190.dp, 850.dp),
                DpOffset(130.dp, 960.dp),
                DpOffset(160.dp, 1080.dp)
            )

            niveles.forEachIndexed { index, nivel ->
                val puedeJugar = nivel.desbloqueado && !nivel.completado

                Box(
                    modifier = Modifier
                        .offset(x = posiciones[index].x, y = posiciones[index].y)
                        .size(60.dp)
                        .background(
                            color = when {
                                nivel.tipo == TipoNivel.MULTIJUGADOR -> if (nivel.completado) Color(0xFF1976D2) else if (nivel.desbloqueado) Color(0xFF64B5F6) else Color.LightGray
                                else -> if (nivel.completado) Color(0xFF388E3C) else if (nivel.desbloqueado) Color(0xFF81C784) else Color.Gray
                            },
                            shape = CircleShape
                        )
                        .clickable(enabled = puedeJugar) {
                            val route = if (nivel.tipo == TipoNivel.MULTIJUGADOR) {
                                "multiplayer_entry/${nivel.id}"
                            } else {
                                "${Screen.SinglePlayer.route}/${nivel.id}"
                            }
                            navController.navigate(route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(nivel.id.toString(), color = Color.White)
                }
            }
        }
    }
}

fun isMultiplayerLevel(id: Int): Boolean {
    return id % 4 == 0
}
