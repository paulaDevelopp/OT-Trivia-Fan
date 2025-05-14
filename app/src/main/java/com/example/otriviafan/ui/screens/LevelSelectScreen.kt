package com.example.otriviafan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.UserViewModel

data class Nivel(val numero: Int, val desbloqueado: Boolean, val superado: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    navController: NavController,
    highestLevelUnlocked: Int,
    userViewModel: UserViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        userViewModel.refreshUserData()
    }

    val niveles = (1..10).map { nivel ->
        Nivel(
            numero = nivel,
            desbloqueado = nivel == highestLevelUnlocked,
            superado = nivel < highestLevelUnlocked
        )
    }

    // Coordenadas manuales para cada botón según el fondo
    val posiciones = listOf(
        Pair(120.dp, 180.dp),
        Pair(220.dp, 340.dp),
        Pair(80.dp, 500.dp),
        Pair(200.dp, 660.dp),
        Pair(100.dp, 820.dp),
        Pair(180.dp, 980.dp),
        Pair(70.dp, 1140.dp),
        Pair(220.dp, 1300.dp),
        Pair(120.dp, 1460.dp),
        Pair(180.dp, 1620.dp)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona un Nivel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8E24AA),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Fondo largo
            Image(
                painter = painterResource(id = R.drawable.ot_sinlogo),
                contentDescription = "Mapa musical OT",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2000.dp),
                contentScale = ContentScale.Crop
            )

            // Botones alineados al mapa
            niveles.forEachIndexed { index, nivel ->
                val (x, y) = posiciones[index]

                Button(
                    onClick = {
                        navController.navigate("${Screen.SinglePlayer.route}/${nivel.numero}")
                    },
                    enabled = nivel.desbloqueado,
                    modifier = Modifier
                        .absoluteOffset(x = x, y = y)
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .zIndex(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            nivel.desbloqueado -> Color(0xFF5E35B1)
                            nivel.superado -> Color(0xFF9E9E9E)
                            else -> Color.Gray
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text(nivel.numero.toString(), fontSize = 18.sp)
                }
            }
        }
    }
}
