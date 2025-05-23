package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun MultiPlayerGameScreen(navController: NavController, matchViewModel: MatchViewModel) {
    val repository = Repository()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val match = matchViewModel.match.collectAsState().value ?: return

    val lastQuestionHandled = remember { mutableStateOf(-1) }
    var youWon by remember { mutableStateOf<Boolean?>(null) }
    val hasAnswered = match.answered[userId] == true
    val currentIndex = match.currentQuestionIndex

    // Avanza a la siguiente pregunta si todos respondieron
    LaunchedEffect(match.answered, currentIndex) {
        val allAnswered = match.answered.values.all { it }
        val isPlayer1 = userId == match.player1Id
        val notAlreadyHandled = currentIndex != lastQuestionHandled.value
        val notFinished = match.status != "finished"

        if (allAnswered && isPlayer1 && notAlreadyHandled && notFinished) {
            lastQuestionHandled.value = currentIndex
            delay(1000)
            repository.nextQuestion(match.matchId)
        }
    }

    // Detectar si ganó, empató  o perdió y desbloquear nivel + sumar puntos
   /* LaunchedEffect(match.status) {
        if (match.status == "finished") {
            /*-Si el jugador gana: suma 20 puntos y pasa de nivel
             -Si empatan: solo pasa de nivel, no suma puntos
             -Si pierde: no hace nada */
            val winner = when {
                match.player1Score > match.player2Score -> match.player1Id
                match.player2Score > match.player1Score -> match.player2Id
                else -> null // empate
            }

            val isWinner = (winner == userId)
            val isDraw = winner == null
            youWon = if (isDraw) null else isWinner

            if (isWinner) {
                repository.addPoints(20)
            }

            if (isWinner || isDraw) {
                repository.marcarNivelCompletado(userId, match.levelName)

                delay(3000)
                navController.navigate(Screen.LevelMap.route) {
                    popUpTo(Screen.LevelMap.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
*/
    // Detectar si ganó, empató o perdió y desbloquear nivel + sumar puntos
    LaunchedEffect(match.status) {
        if (match.status == "finished") {
            val player1Score = match.player1Score
            val player2Score = match.player2Score

            val isZeroZero = player1Score == 0 && player2Score == 0
            val isDraw = player1Score == player2Score && !isZeroZero

            val winner = when {
                player1Score > player2Score -> match.player1Id
                player2Score > player1Score -> match.player2Id
                isDraw -> null
                else -> null // caso 0-0
            }

            val isWinner = (winner == userId)

            youWon = when {
                isWinner -> true
                isDraw -> null // empate sin ganador
                isZeroZero -> false // nadie ganó
                else -> false
            }

            // Solo suma puntos si ganó
            if (isWinner) {
                repository.addPoints(20)
            }

            // Solo marca nivel como completado si ganó o empató (no en 0-0)
            if (isWinner || isDraw) {
                repository.marcarNivelCompletado(userId, match.levelName)
            }

            delay(3000)
            navController.navigate(Screen.LevelMap.route) {
                popUpTo(Screen.LevelMap.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }



    // UI (igual que antes)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Modo multijugador", style = MaterialTheme.typography.headlineMedium)

        val questions = match.questions

        if (match.status == "finished" && youWon != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (youWon == true) "¡Ganaste 20 puntos!" else "Has perdido esta vez...",
                style = MaterialTheme.typography.headlineLarge
            )
            if (youWon == false) {
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
        } else if (questions.isNotEmpty() && currentIndex < questions.size) {
            val question = questions[currentIndex]

            if (question.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = question.imageUrl,
                    contentDescription = "Imagen de la pregunta",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            //Se muestra la pregunta actual.
            Text("Pregunta ${currentIndex + 1} / ${questions.size}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(question.questionText, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            question.answers.forEach { answer ->
                Button(
                    onClick = {
                        if (!hasAnswered) {
                            scope.launch {

                                repository.setPlayerAnswered(match.matchId, userId, answer.id)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    enabled = !hasAnswered
                ) {
                    Text(answer.answerText)
                }
            }

            if (hasAnswered) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Esperando al otro jugador...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Marcador", style = MaterialTheme.typography.titleMedium)
        Text("Tú: ${if (userId == match.player1Id) match.player1Score else match.player2Score}")
        Text("Oponente: ${if (userId == match.player1Id) match.player2Score else match.player1Score}")
    }
}
