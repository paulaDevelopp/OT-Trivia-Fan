package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.otriviafan.R
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.otriviafan.ui.theme.LuckiestGuyFont

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
    val showCountdown = remember { mutableStateOf(false) }
    val remainingTime = remember { mutableStateOf(10) }
    val showAbandonDialog = remember { mutableStateOf(false) }
    val sizes = rememberResponsiveSizes()

    // Ping de actividad
    LaunchedEffect(userId) {
        while (true) {
            delay(5000)
            repository.updateLastActive(match.matchId, userId)
        }
    }

    // Lógica de pregunta y cuenta atrás
    LaunchedEffect(match.answered, currentIndex) {
        val allAnswered = match.answered.values.all { it }
        val notHandled = currentIndex != lastQuestionHandled.value
        val notFinished = match.status != "finished"

        if (allAnswered && notHandled && notFinished) {
            lastQuestionHandled.value = currentIndex
            repository.nextQuestion(match.matchId)
        }

        val now = System.currentTimeMillis()
        val rivalId = if (userId == match.player1Id) match.player2Id else match.player1Id
        val rivalLastActive = match.lastActive[rivalId]

        if (rivalLastActive != null && notFinished) {
            val timeDiff = now - rivalLastActive

            when {
                timeDiff > 40000 -> {
                    val player1Score = if (userId == match.player1Id) 1 else 0
                    val player2Score = if (userId == match.player2Id) 1 else 0

                    scope.launch {
                        repository.finishMatchDueToInactivity(match.matchId, player1Score, player2Score)
                    }
                }

                timeDiff > 40000 && !showCountdown.value -> {
                    showCountdown.value = true
                    remainingTime.value = 10
                    scope.launch {
                        for (i in 10 downTo 1) {
                            val latest = matchViewModel.match.value?.lastActive?.get(rivalId)
                            if (latest != null && System.currentTimeMillis() - latest < 10000) {
                                showCountdown.value = false
                                break
                            }
                            remainingTime.value = i
                            delay(1000)
                        }
                        showCountdown.value = false
                    }
                }

                timeDiff < 10000 && showCountdown.value -> {
                    showCountdown.value = false
                }
            }
        }
    }



    // Fin de partida
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

            delay(1000)
            navController.navigate(Screen.MultiPlayerResult.route) {
                popUpTo(Screen.MultiPlayerGame.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo__home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
        )

        if (showAbandonDialog.value) {
            AlertDialog(
                onDismissRequest = { showAbandonDialog.value = false },
                title = { Text("¿Seguro que quieres abandonar?") },
                text = { Text("Si abandonas, la partida se terminará y nadie ganará puntos.") },
                confirmButton = {
                    TextButton(onClick = {
                        showAbandonDialog.value = false
                        scope.launch {
                            repository.abandonMatch(match.matchId)
                            delay(300)
                            navController.navigate(Screen.LevelMap.route) {
                                popUpTo(Screen.LevelMap.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Text("Sí, abandonar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAbandonDialog.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "¡Qué gane el mejor!",
                    fontFamily = LuckiestGuyFont,
                    fontSize = sizes.fontSizeLarge,
                    color = Color.White
                )
                IconButton(onClick = { showAbandonDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Abandonar partida",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Marcador", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Tú: ${if (userId == match.player1Id) match.player1Score else match.player2Score}", color = Color.White)
            Text("Oponente: ${if (userId == match.player1Id) match.player2Score else match.player1Score}", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            if (showCountdown.value) {
                Text(
                    "⚠ El rival está inactivo. La partida terminará en ${remainingTime.value} segundos...",
                    fontSize = sizes.fontSizeSmall,
                    color = Color.Yellow,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val question = match.questions.getOrNull(currentIndex)
            if (question != null) {
                if (question.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = question.imageUrl,
                        contentDescription = "Imagen de la pregunta",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(sizes.screenHeight * 0.28f) // En lugar de 220.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Pregunta ${currentIndex + 1} / ${match.questions.size}", color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = question.questionText,
                    fontSize = sizes.fontSizeMedium,
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
                            .height(sizes.buttonHeight)
                            .padding(vertical = sizes.screenHeight * 0.01f)
                    ) {
                        Text(answer.answerText, fontSize = sizes.fontSizeMedium)
                    }
                }

                if (hasAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando al otro jugador...", color = Color.White.copy(alpha = 0.7f))
                }
            }


        }
    }
}
