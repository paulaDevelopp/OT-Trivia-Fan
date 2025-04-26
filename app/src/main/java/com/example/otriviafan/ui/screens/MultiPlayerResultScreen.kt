package com.example.otriviafan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MultiPlayerResultScreen(
    navController: NavController,
    matchViewModel: MatchViewModel
) {
    val match = matchViewModel.match.collectAsState().value ?: return
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val myScore = if (uid == match.player1Id) match.player1Score else match.player2Score
    val opponentScore = if (uid == match.player1Id) match.player2Score else match.player1Score

    val resultText = when {
        myScore > opponentScore -> "🎉 ¡Ganaste la partida!"
        myScore < opponentScore -> "😓 Perdiste... ¡la próxima será!"
        else -> "🤝 ¡Empate!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Partida finalizada", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tu puntuación: $myScore", style = MaterialTheme.typography.titleMedium)
        Text("Oponente: $opponentScore", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(resultText, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }) {
            Text("Volver al inicio")
        }
    }
}
