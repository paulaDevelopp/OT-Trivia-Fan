package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MultiPlayerWaitingScreen(
    navController: NavController,
    matchViewModel: MatchViewModel
) {
    val match = matchViewModel.match.collectAsState().value
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        matchViewModel.createMatch(uid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Esperando a otro jugador...", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Código de partida:", style = MaterialTheme.typography.bodyMedium)
        Text(match?.matchId ?: "Cargando...", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        if (match?.status == "active") {
            // Ya hay un segundo jugador, se puede empezar
            LaunchedEffect(match.status) {
                navController.navigate(Screen.MultiPlayerGame.route)
            }
        }

        CircularProgressIndicator()
    }
}
