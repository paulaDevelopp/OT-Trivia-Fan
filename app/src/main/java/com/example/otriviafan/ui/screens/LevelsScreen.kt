package com.example.otriviafan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otriviafan.data.model.Nivel
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(navController: NavController) {
    val userViewModel: UserViewModel = viewModel()
    val highestLevelUnlocked by userViewModel.highestLevelUnlocked.collectAsState()

    val totalNiveles = 10
    val niveles = List(totalNiveles) { index ->
        Nivel(numero = index + 1, desbloqueado = (index + 1) <= highestLevelUnlocked)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona un Nivel") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF8E24AA))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(niveles) { nivel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable(enabled = nivel.desbloqueado) {
                                navController.navigate("level_game/${nivel.numero}")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (nivel.desbloqueado) Color(0xFF3949AB) else Color.Gray
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Nivel ${nivel.numero}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
*/