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
    Image(
        painter = painterResource(id = R.drawable.fondo_home_),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.height(1500.dp)) {

            Image(
                painter = painterResource(id = R.drawable.camino),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()


            )
            val posiciones = listOf(
                DpOffset(140.dp, 80.dp),
                DpOffset(90.dp, 200.dp),
                DpOffset(200.dp, 320.dp),
                DpOffset(100.dp, 440.dp),
                DpOffset(180.dp, 560.dp),
                DpOffset(100.dp, 680.dp),
                DpOffset(190.dp, 800.dp),
                DpOffset(120.dp, 920.dp),
                DpOffset(160.dp, 1040.dp),
                DpOffset(130.dp, 1160.dp)
            )

            niveles.forEachIndexed { index, nivel ->
                if (index >= posiciones.size) return@forEachIndexed
                val puedeJugar = nivel.desbloqueado && !nivel.completado
                val levelName = levelNames[nivel.id] ?: return@forEachIndexed

                val iconResId = when {
                    nivel.tipo == TipoNivel.INDIVIDUAL && nivel.completado -> R.drawable.individual_superado
                    nivel.tipo == TipoNivel.INDIVIDUAL && puedeJugar -> R.drawable.individual_actual
                    nivel.tipo == TipoNivel.INDIVIDUAL -> R.drawable.multijugador_bloqueado
                    nivel.tipo == TipoNivel.MULTIJUGADOR && nivel.completado -> R.drawable.multijugador_ganado
                    nivel.tipo == TipoNivel.MULTIJUGADOR && puedeJugar -> R.drawable.multijugador_actual
                    else -> R.drawable.multi_bloqueado
                }


                Box(
                    modifier = Modifier
                        .offset(x = posiciones[index].x, y = posiciones[index].y)
                        .size(80.dp)
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
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = "Nivel ${nivel.id}",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

        }
        }
    }

