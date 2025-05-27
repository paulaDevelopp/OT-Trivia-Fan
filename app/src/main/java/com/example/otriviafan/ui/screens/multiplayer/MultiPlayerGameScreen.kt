// This is a prettified version of the multiplayer game screen using your shared styles
// It applies background, layout alignment, spacing, and text styles consistently

package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.otriviafan.R
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

    LaunchedEffect(match.answered, currentIndex) {
        val allAnswered = match.answered.values.all { it }
        val isPlayer1 = userId == match.player1Id
        val notHandled = currentIndex != lastQuestionHandled.value
        val notFinished = match.status != "finished"

        if (allAnswered && isPlayer1 && notHandled && notFinished) {
            lastQuestionHandled.value = currentIndex
            delay(1000)
            repository.nextQuestion(match.matchId)
        }
    }

    LaunchedEffect(match.status) {
        if (match.status == "finished") {
            val player1Score = match.player1Score
            val player2Score = match.player2Score

            val isZeroZero = player1Score == 0 && player2Score == 0
            val isDraw = player1Score == player2Score && !isZeroZero
            val winner = when {
                player1Score > player2Score -> match.player1Id
                player2Score > player1Score -> match.player2Id
                else -> null
            }

            val isWinner = winner == userId
            youWon = when {
                isWinner -> true
                isDraw -> null
                isZeroZero -> false
                else -> false
            }

            if (isWinner) repository.addPoints(20)
            if (isWinner || isDraw) repository.marcarNivelCompletado(userId, match.levelName)

            delay(3000)
            navController.navigate(Screen.LevelMap.route) {
                popUpTo(Screen.LevelMap.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo__home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Modo multijugador", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            if (match.status == "finished" && youWon != null) {
                Text(
                    if (youWon == true) "\uD83C\uDFC6 ¡Ganaste 20 puntos!" else "\uD83D\uDE14 Has perdido esta vez...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            } else {
                val question = match.questions.getOrNull(currentIndex)
                if (question != null) {
                    if (question.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = question.imageUrl,
                            contentDescription = "Imagen de la pregunta",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        "Pregunta ${currentIndex + 1} / ${match.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    question.answers.forEach { answer ->
                        Button(
                            onClick = {
                                if (!hasAnswered) scope.launch {
                                    repository.setPlayerAnswered(match.matchId, userId, answer.id)
                                }
                            },
                            enabled = !hasAnswered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(answer.answerText)
                        }
                    }

                    if (hasAnswered) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Esperando al otro jugador...", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Marcador", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Tú: ${if (userId == match.player1Id) match.player1Score else match.player2Score}", color = Color.White)
            Text("Oponente: ${if (userId == match.player1Id) match.player2Score else match.player1Score}", color = Color.White)
        }
    }
}
