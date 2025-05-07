package com.example.otriviafan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(niveles) { nivel ->
                val backgroundColor = when {
                    nivel.desbloqueado -> Color(0xFF5E35B1)
                    nivel.superado -> Color(0xFF9E9E9E)
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = nivel.desbloqueado) {
                            navController.navigate("${Screen.SinglePlayer.route}/${nivel.numero}")
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nivel ${nivel.numero}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (nivel.superado) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completado",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
