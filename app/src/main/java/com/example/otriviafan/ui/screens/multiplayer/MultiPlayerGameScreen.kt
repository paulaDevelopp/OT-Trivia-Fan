package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.Match
import com.example.otriviafan.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MultiPlayerGameScreen(navController: NavController, nivelId: Int) {
    val repository = Repository()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var match by remember { mutableStateOf<Match?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var gameOver by remember { mutableStateOf(false) }
    var youWon by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Cargar preguntas del nivel multijugador correspondiente
            val questions = repository.getQuestionsForMultiplayerLevel(nivelId)

            // Crear match con esas preguntas
            val matchId = repository.createMatchWithQuestions(userId, questions)

            // Observar partida
            repository.observeMatch(matchId) { updated ->
                match = updated

                val bothAnswered = updated.answered.values.all { it }
                if (bothAnswered && !gameOver) {
                    gameOver = true
                    val winner = when {
                        updated.player1Score > updated.player2Score -> updated.player1Id
                        updated.player2Score > updated.player1Score -> updated.player2Id
                        else -> null
                    }

                    youWon = winner == userId

                    if (youWon == true) {
                        scope.launch {
                            repository.marcarNivelCompletado(userId, nivelId, "multijugador")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Modo multijugador", style = MaterialTheme.typography.headlineMedium)

        if (gameOver && youWon != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (youWon == true) "¡Ganaste!" else "Has perdido esta vez...",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                navController.navigate(Screen.LevelMap.route) {
                    popUpTo(Screen.LevelMap.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text("Volver al mapa")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
