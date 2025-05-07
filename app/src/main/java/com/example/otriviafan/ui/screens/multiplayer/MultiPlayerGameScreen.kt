package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun MultiPlayerGameScreen(
    navController: NavController,
    matchViewModel: MatchViewModel
) {
    val match = matchViewModel.match.collectAsState().value
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val currentMatch = match ?: return

    val questionIndex = currentMatch.currentQuestionIndex
    val question = currentMatch.questions.getOrNull(questionIndex)
    if (match.status == "finished") {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.MultiPlayerResult.route) {
                popUpTo(Screen.MultiPlayerGame.route) { inclusive = true }
            }
        }
    }
    if (question == null) {
        Text("No hay preguntas disponibles.")
        return
    }

    val alreadyAnswered = currentMatch.answered[userId] == true
    val answeredAll = currentMatch.answered.values.all { it }
    val youAreWinner = currentMatch.currentWinner == userId

    // Estado para controlar si mostramos el resultado temporalmente
    var showResult by remember { mutableStateOf(false) }

    // Si todos respondieron y no estamos mostrando resultado aún
    LaunchedEffect(answeredAll) {
        if (answeredAll && !showResult) {
            showResult = true
            delay(1500) // ⏳ Esperamos 1.5 segundos
            matchViewModel.nextQuestion()
            showResult = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de progreso
        LinearProgressIndicator(
            progress = (questionIndex + 1) / currentMatch.questions.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Pregunta ${questionIndex + 1} de ${currentMatch.questions.size}", style = MaterialTheme.typography.titleMedium)

        Text(text = question.questionText, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        question.answers.shuffled().forEach { answer ->
            Button(
                onClick = {
                    if (!alreadyAnswered) {
                        matchViewModel.sendAnswer(answer.id)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !alreadyAnswered
            ) {
                Text(answer.answerText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showResult && currentMatch.currentWinner != null) {
            if (youAreWinner) {
                Text(
                    "¡Ganaste esta pregunta! 🎉",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineSmall
                )
            } else {
                Text(
                    "El otro jugador respondió primero 😢",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        } else if (alreadyAnswered && !answeredAll) {
            Text("Esperando al otro jugador...", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Marcador", style = MaterialTheme.typography.titleMedium)
        Text("Tú: ${if (userId == currentMatch.player1Id) currentMatch.player1Score else currentMatch.player2Score}")
        Text("Oponente: ${if (userId == currentMatch.player1Id) currentMatch.player2Score else currentMatch.player1Score}")
    }
}
