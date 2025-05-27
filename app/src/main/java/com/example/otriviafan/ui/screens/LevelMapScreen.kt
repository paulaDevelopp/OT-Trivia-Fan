package com.example.otriviafan.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.NivelUI
import com.example.otriviafan.data.model.TipoNivel
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelMapScreen(navController: NavController, userViewModel: UserViewModel) {
    val scope = rememberCoroutineScope()
    var niveles by remember { mutableStateOf<List<NivelUI>>(emptyList()) }
    var levelNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showHelp by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { showHelp = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HelpOutline,
                                contentDescription = "Ayuda",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¿Cómo se juega?",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Image(
                painter = painterResource(id = R.drawable.fondo_home_),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.height(1500.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.camino),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    val posiciones = listOf(
                        DpOffset(140.dp, 80.dp), DpOffset(90.dp, 200.dp),
                        DpOffset(200.dp, 320.dp), DpOffset(100.dp, 440.dp),
                        DpOffset(180.dp, 560.dp), DpOffset(100.dp, 680.dp),
                        DpOffset(190.dp, 800.dp), DpOffset(120.dp, 920.dp),
                        DpOffset(160.dp, 1040.dp), DpOffset(130.dp, 1160.dp)
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

            if (showHelp) {
                HelpOverlay { showHelp = false }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color(0xFF80D8FF)) {
        NavigationBarItem(
            icon = { Text("🏠", fontSize = 20.sp) },
            label = { Text("Inicio", fontSize = 12.sp) },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Text("🛍️", fontSize = 20.sp) },
            label = { Text("Tienda", fontSize = 12.sp) },
            selected = false,
            onClick = { navController.navigate("store") }
        )
        NavigationBarItem(
            icon = { Text("👤", fontSize = 20.sp) },
            label = { Text("Perfil", fontSize = 12.sp) },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HelpOverlay(onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(420.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cómo se juega a OTRIVIA FAN?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> Text(
                            text = """
🎯 Modo Individual:

✅ 1. Juega solo, sin presión.
🎯 2. Responde preguntas para sumar puntos.
🔓 3. Desbloquea niveles uno a uno.
❤️ 4. Pierdes vidas al fallar.
🔁 5. Usa puntos para reintentar.
🏆 6. ¡Haz partidas perfectas y gana recompensas!
                            """.trimIndent(),
                            fontSize = 16.sp
                        )
                        1 -> Text(
                            text = """
🤝 Modo Multijugador:

👥 1. Juega en tiempo real contra otro usuario.
⚡ 2. El primero en responder gana el punto.
📊 3. Se lleva la victoria quien acumule más puntos.
🔥 4. Agilidad y precisión son clave.
🎁 5. Gana recompensas si ganas o empatas.
                            """.trimIndent(),
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("⬅️ Desliza para ver más ➡️", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(2) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .padding(4.dp)
                                .background(
                                    color = if (selected) Color(0xFF80D8FF) else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80D8FF))
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}
