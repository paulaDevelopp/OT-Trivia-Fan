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
import com.example.otriviafan.data.model.NivelUI
import com.example.otriviafan.data.model.TipoNivel
@Composable
fun LevelMapScreen(navController: NavController, userViewModel: UserViewModel) {
    val scope = rememberCoroutineScope()
    var niveles by remember { mutableStateOf<List<NivelUI>>(emptyList()) }
    var levelNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    LaunchedEffect(Unit) {
        scope.launch {
            val userId = userViewModel.getUserId()

            // ✅ REFRESCA LOS DATOS DEL USUARIO DESDE FIREBASE
            userViewModel.refreshUserData()

            val progreso = userViewModel.getNivelProgreso(userId)
            val repository = Repository()
            val allLevelNames = repository.getAllLevelNamesOrdered()

            val nuevosNiveles = mutableListOf<NivelUI>()
            var desbloquear = true
            var multiplayerPendiente = false

            allLevelNames.forEachIndexed { index, levelName ->
                val completado = progreso[levelName]?.completado == true
                val tipo = if (repository.esNivelMultijugador(levelName)) TipoNivel.MULTIJUGADOR else TipoNivel.INDIVIDUAL

                val desbloqueado = when {
                    index == 0 -> true
                    tipo == TipoNivel.MULTIJUGADOR -> desbloquear
                    else -> desbloquear && !multiplayerPendiente
                }

                if (tipo == TipoNivel.MULTIJUGADOR) {
                    multiplayerPendiente = !completado
                } else {
                    desbloquear = completado
                }

                nuevosNiveles.add(
                    NivelUI(
                        id = index + 1,
                        tipo = tipo,
                        desbloqueado = desbloqueado,
                        completado = completado
                    )
                )
            }

            niveles = nuevosNiveles
            levelNames = allLevelNames.mapIndexed { i, name -> (i + 1) to name }.toMap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.height(1500.dp)) {
            Image(
                painter = painterResource(id = R.drawable.fondoniveles),
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
                if (index >= posiciones.size) return@forEachIndexed
                val puedeJugar = nivel.desbloqueado && !nivel.completado
                val levelName = levelNames[nivel.id] ?: return@forEachIndexed

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
                                "multiplayer_entry/$levelName"
                            } else {
                                "${Screen.SinglePlayer.route}/$levelName"
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
